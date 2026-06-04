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
import play.api.db.Database
import uk.gov.hmrc.rdscandeproxy.euvat.models.responses.TradersKnownFacts

import java.sql.{CallableStatement, ResultSet}
import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait EuVatCandeDataSource {
  def getTraderByVrn(vrn: String): Future[TradersKnownFacts]
}
class EuVatCandeRepository @Inject() (db: Database)(implicit ec: ExecutionContext) extends EuVatCandeDataSource with Logging {

  def getTraderByVrn(vrn: String): Future[TradersKnownFacts] = {
    logger.info(s"************* calling SP getTraderByVrn - VRN: $vrn")
    Future {
      db.withConnection { connection =>
        val knownFactsStatement: CallableStatement = connection.prepareCall("{call EUVAT_FILING_DC_KF.getTraderByVRN(?, ?)}")

        try {
          // Set input parameters
          knownFactsStatement.setInt("p_vrn", vrn.toInt) // VRN
          // Register output parameters
          knownFactsStatement.registerOutParameter("p_trader", OracleTypes.CURSOR) // p_trader
          // Execute the stored procedure
          knownFactsStatement.execute()
          // Retrieve output parameters
          val knownFactsResult = knownFactsStatement.getObject("p_trader", classOf[ResultSet]) // p_trader (REF CURSOR)
          logger.info(s"***** DB response from SQL stored procedure: $knownFactsResult")

          try {
            if (knownFactsResult.next()) {
              TradersKnownFacts(
                vatRegNumber = knownFactsResult.getInt("vat_reg_number"),
                traderName   = Option(knownFactsResult.getString("trader_name")).orNull,
                addressLine1 = Option(knownFactsResult.getString("bus_address_1")).orNull,
                addressLine2 = Option(knownFactsResult.getString("bus_address_2")).orNull,
                addressLine3 = Option(knownFactsResult.getString("bus_address_3")).orNull,
                addressLine4 = Option(knownFactsResult.getString("bus_address_4")).orNull,
                addressLine5 = Option(knownFactsResult.getString("bus_address_5")).orNull,
                postCode     = Option(knownFactsResult.getString("bus_postcode")).orNull,
                tradeClass   = Option(knownFactsResult.getString("trade_class")).orNull,
                dateOfRegistration = Option(knownFactsResult.getTimestamp("date_of_reg"))
                  .map(_.toLocalDateTime)
                  .getOrElse(LocalDateTime.MIN),
                dateOfDeregistration = Option(knownFactsResult.getTimestamp("date_of_dereg"))
                  .map(_.toLocalDateTime)
                  .getOrElse(LocalDateTime.MIN),
                missingTraderIndicator = Option(knownFactsResult.getString("missing_trader_ind")).orNull,
                singleMarketIndicator  = knownFactsResult.getInt("ph_sem_trader_ind")
              )
            } else {
              logger.error("********* getTraderByVrn - issue retrieving known facts from rds cande database")
              null
            }
          } finally knownFactsResult.close()
        } finally knownFactsStatement.close()

      }
    }
  }
}
