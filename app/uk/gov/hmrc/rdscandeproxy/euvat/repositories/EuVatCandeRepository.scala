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
import uk.gov.hmrc.rdscandeproxy.euvat.models.requests.LatestApplicationRequest
import uk.gov.hmrc.rdscandeproxy.euvat.models.responses.{LatestApplication, LatestApplicationResponse, TradersKnownFacts}

import java.sql.ResultSet
import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Using

trait EuVatCandeDataSource {
  def getTraderByVrn(vrn: String): Future[Option[TradersKnownFacts]]
  def getLatestApplications(request: LatestApplicationRequest): Future[LatestApplicationResponse]
}
class EuVatCandeRepository @Inject() (db: Database)(implicit ec: ExecutionContext) extends EuVatCandeDataSource with Logging {

  def getTraderByVrn(vrn: String): Future[Option[TradersKnownFacts]] = {
    logger.info(s"************* calling stored procedure getTraderByVrn for VRN: $vrn")
    Future {
      db.withConnection { connection =>
        Using.resource(connection.prepareCall("{call EUVAT_FILING_DC_KF.getTraderByVRN(?, ?)}")) { stmt =>
          stmt.setInt("p_vrn", vrn.toInt)
          stmt.registerOutParameter("p_trader", OracleTypes.CURSOR)

          stmt.execute()

          val rs = stmt.getObject("p_trader", classOf[ResultSet])
          Using.resource(rs) { cursor =>
            if (!cursor.next()) {
              logger.warn(s"No trader known facts returned from stored procedure for vrn: $vrn")
              None
            } else {
              Some(
                TradersKnownFacts(
                  vatRegNumber           = cursor.getInt("vat_reg_number"),
                  traderName             = Option(cursor.getString("trader_name")).orNull,
                  addressLine1           = Option(cursor.getString("bus_address_1")).orNull,
                  addressLine2           = Option(cursor.getString("bus_address_2")).orNull,
                  addressLine3           = Option(cursor.getString("bus_address_3")).orNull,
                  addressLine4           = Option(cursor.getString("bus_address_4")).orNull,
                  addressLine5           = Option(cursor.getString("bus_address_5")).orNull,
                  postCode               = Option(cursor.getString("bus_postcode")).orNull,
                  tradeClass             = Option(cursor.getString("trade_class")).getOrElse(""),
                  dateOfRegistration     = Option(cursor.getTimestamp("date_of_reg")).map(_.toLocalDateTime).getOrElse(LocalDateTime.MIN),
                  dateOfDeregistration   = Option(cursor.getTimestamp("date_of_dereg")).map(_.toLocalDateTime).getOrElse(LocalDateTime.MIN),
                  missingTraderIndicator = Option(cursor.getString("missing_trader_ind")).orNull,
                  singleMarketIndicator  = cursor.getInt("ph_sem_trader_ind")
                )
              )
            }
          }
        }

      }

    }
  }

  def getLatestApplications(request: LatestApplicationRequest): Future[LatestApplicationResponse] = {
    logger.info(s"Calling stored procedure getLatestApplications for VRN: ${request.applicantVatRegNumber}")
    Future {
      db.withConnection { connection =>
        Using.resource(connection.prepareCall("{call EUVAT_FILE_DATA.EU_VAT_RETRIEVAL.getLatestApplications(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {
          storedProcedure =>

            // Set input parameters
            storedProcedure.setString("p_applicant_vat_reg_number", request.applicantVatRegNumber)
            request.refundingCountry match {
              case Some(country) => storedProcedure.setString("p_refunding_country", country)
              case None          => storedProcedure.setNull("p_refunding_country", java.sql.Types.VARCHAR)
            }

            request.startDate match {
              case Some(date) => storedProcedure.setDate("p_start_date", java.sql.Date.valueOf(date.toLocalDate))
              case None       => storedProcedure.setNull("p_start_date", java.sql.Types.DATE)
            }

            request.endDate match {
              case Some(date) => storedProcedure.setDate("p_end_date", java.sql.Date.valueOf(date.toLocalDate))
              case None       => storedProcedure.setNull("p_end_date", java.sql.Types.DATE)
            }
            request.representativeId match {
              case Some(repId) => storedProcedure.setString("p_representative_id", repId)
              case None        => storedProcedure.setNull("p_representative_id", java.sql.Types.VARCHAR)
            }
            storedProcedure.setObject("p_order_by", request.orderBy.orNull)
            storedProcedure.setObject("p_sort_order", request.sortOrder.orNull)
            storedProcedure.setObject("p_start_at", request.startAt.getOrElse(0))
            storedProcedure.setInt("p_max_number", request.maxNumber)

            // Register output parameters
            storedProcedure.registerOutParameter("p_applications", OracleTypes.CURSOR)
            storedProcedure.registerOutParameter("p_total_applications", OracleTypes.NUMBER)

            // Execute
            storedProcedure.execute()

            // Retrieve output parameters
            val totalApplications = storedProcedure.getInt("p_total_applications")
            val rs = storedProcedure.getObject("p_applications", classOf[ResultSet])

            Using.resource(rs) { cursor =>
              val applications =
                Iterator
                  .continually(cursor.next())
                  .takeWhile(identity)
                  .map(_ =>
                    LatestApplication(
                      applicationId        = cursor.getLong("application_id"),
                      refundingCountryCode = cursor.getString("refunding_country_code"),
                      periodStartDate      = cursor.getTimestamp("period_start_date").toLocalDateTime,
                      periodEndDate        = cursor.getTimestamp("period_end_date").toLocalDateTime,
                      applicationNumber    = cursor.getString("application_number"),
                      applicationStatus    = cursor.getString("application_status"),
                      submissionStatus     = cursor.getString("submission_status"),
                      applicationVersion   = cursor.getTimestamp("application_version").toLocalDateTime
                    )
                  )
                  .toList
              LatestApplicationResponse(applications, totalApplications)
            }
        }
      }
    }
  }
}
