/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.rdscandeproxy.euvat.repositories

import oracle.jdbc.OracleTypes
import play.api.Logging
import play.api.db.{Database, NamedDatabase}
import uk.gov.hmrc.rdscandeproxy.euvat.models.requests.ApplicationRequest
import uk.gov.hmrc.rdscandeproxy.euvat.models.responses.ApplicationResponse

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Using

class EuVatCandeRepository @Inject() (@NamedDatabase("euvat") db: Database)(using ec: ExecutionContext) extends Logging {

  def addApplication(applicationRequest: ApplicationRequest): Future[ApplicationResponse] = {
    logger.info(s"************* calling stored procedure addApplication for VRN: ${applicationRequest.applicantVatRegNumber}")
    Future {
      db.withConnection { connection =>
        Using.resource(connection.prepareCall("{call EUVAT_FILE_DATA.EU_VAT_UPDATE.addApplication(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
          stmt =>
            // Set input parameters
            stmt.setString("p_applicant_vat_reg_number", applicationRequest.applicantVatRegNumber)
            stmt.setString("p_refunding_country_code", applicationRequest.refundingCountryCode.orNull)
            stmt.setTimestamp("p_period_start_date", applicationRequest.periodStartDate.map(java.sql.Timestamp.valueOf).orNull)
            stmt.setTimestamp("p_period_end_date", applicationRequest.periodEndDate.map(java.sql.Timestamp.valueOf).orNull)
            stmt.setString("p_applicant_email_address", applicationRequest.applicantEmailAddress.orNull)
            stmt.setString("p_applicant_telephone_num", applicationRequest.applicantTelephoneNumber.orNull)
            stmt.setString("p_application_language", applicationRequest.applicationLanguage.orNull)
            stmt.setString("p_representative_id", applicationRequest.representativeId.orNull)
            stmt.setString("p_representative_country", applicationRequest.representativeCountryCode.orNull)
            stmt.setString("p_representative_email_address", applicationRequest.representativeEmailAddress.orNull)
            stmt.setString("p_representative_id_type", applicationRequest.representativeIdType.orNull)
            stmt.setString("p_representative_telephone_num", applicationRequest.representativeTelephoneNumber.orNull)
            stmt.setString("p_bank_account_owner_name", applicationRequest.bankAccountOwnerName.orNull)
            stmt.setString("p_bank_account_owner_type", applicationRequest.bankAccountOwnerType.orNull)
            stmt.setString("p_iban_code", applicationRequest.iBanCode.orNull)
            stmt.setString("p_bic_code", applicationRequest.bicCode.orNull)
            stmt.setString("p_bank_account_currency_code", applicationRequest.bankAccountCurrencyCode.orNull)
            stmt.setString("p_business_activity_code1", applicationRequest.businessActivityCode1.orNull)
            stmt.setString("p_business_activity_code2", applicationRequest.businessActivityCode2.orNull)
            stmt.setString("p_business_activity_code3", applicationRequest.businessActivityCode3.orNull)
            stmt.registerOutParameter("p_application_id", OracleTypes.NUMBER)
            stmt.registerOutParameter("p_application_number", OracleTypes.VARCHAR)
            stmt.registerOutParameter("p_update_seq_number", OracleTypes.NUMBER)

            stmt.execute()
            logger.info("Data successfully saved in database")

            ApplicationResponse(
              stmt.getInt("p_application_id"),
              stmt.getString("p_application_number"),
              stmt.getInt("p_update_seq_number")
            )
        }
      }
    }
  }

}
