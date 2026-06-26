/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import uk.gov.hmrc.rdscandeproxy.euvat.config.AppConfig
import uk.gov.hmrc.rdscandeproxy.euvat.models.requests.LatestApplicationRequest

import java.sql.{CallableStatement, Connection, ResultSet}
import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class EuVatCandeRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: EuVatCandeRepository = _
  var mockConnection: Connection = _
  var mockCallableStatement: CallableStatement = _
  var mockResultSet: ResultSet = _
  var mockConfig: AppConfig = _

  before {
    // Mocking the database connection and callable statement
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[Connection])
    mockCallableStatement = mock(classOf[CallableStatement])
    mockResultSet         = mock(classOf[ResultSet])
    mockConfig            = mock(classOf[AppConfig])

    // When db.withConnection is called, it should invoke the passed-in function and return the result
    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[Connection => Any])
      func(mockConnection) // Return the result of the lambda function passed to withConnection
    }

    // When prepareCall is invoked on the connection, return the mocked callable statement
    when(mockConnection.prepareCall(any[String])).thenReturn(mockCallableStatement)

    // Initialize the repository with the mocked db connection
    repository = new EuVatCandeRepository(db)
  }

  "getLatestApplications" should "return a LatestApplicationResponse with correct data" in {
    val request = LatestApplicationRequest(
      applicantVatRegNumber = "123456789",
      refundingCountry      = Some("LV"),
      startDate             = Some(LocalDateTime.of(2025, 2, 1, 0, 0)),
      endDate               = Some(LocalDateTime.of(2025, 5, 31, 0, 0)),
      representativeId      = Some("rep123"),
      maxNumber             = 10,
      orderBy               = None,
      sortOrder             = None,
      startAt               = None
    )

    when(mockCallableStatement.getObject("p_applications", classOf[ResultSet])).thenReturn(mockResultSet)
    when(mockCallableStatement.getInt("p_total_applications")).thenReturn(1)

    when(mockResultSet.next()).thenReturn(true, false)
    when(mockResultSet.getLong("application_id")).thenReturn(133L)
    when(mockResultSet.getString("refunding_country_code")).thenReturn("LV")
    when(mockResultSet.getTimestamp("period_start_date")).thenReturn(java.sql.Timestamp.valueOf(LocalDateTime.of(2025, 2, 1, 0, 0)))
    when(mockResultSet.getTimestamp("period_end_date")).thenReturn(java.sql.Timestamp.valueOf(LocalDateTime.of(2025, 5, 31, 23, 59)))
    when(mockResultSet.getString("application_number")).thenReturn("GB0000000000000133")
    when(mockResultSet.getString("application_status")).thenReturn("D")
    when(mockResultSet.getString("submission_status")).thenReturn("S")
    when(mockResultSet.getTimestamp("application_version")).thenReturn(java.sql.Timestamp.valueOf(LocalDateTime.of(2025, 2, 11, 10, 38)))

    val result = repository.getLatestApplications(request).futureValue

    result.totalApplication                       shouldBe 1
    result.applications.head.applicationId        shouldBe 133L
    result.applications.head.refundingCountryCode shouldBe "LV"
  }

  "getLatestApplications" should "return empty list when no applications found" in {
    val request = LatestApplicationRequest(
      applicantVatRegNumber = "123456789",
      refundingCountry      = Some("LV"),
      startDate             = Some(LocalDateTime.of(2025, 2, 1, 0, 0)),
      endDate               = Some(LocalDateTime.of(2025, 5, 31, 0, 0)),
      representativeId      = Some("rep123"),
      maxNumber             = 10,
      orderBy               = None,
      sortOrder             = None,
      startAt               = None
    )

    when(mockCallableStatement.getObject("p_applications", classOf[ResultSet])).thenReturn(mockResultSet)
    when(mockCallableStatement.getInt("p_total_applications")).thenReturn(0)
    when(mockResultSet.next()).thenReturn(false)

    val result = repository.getLatestApplications(request).futureValue

    result.totalApplication shouldBe 0
    result.applications     shouldBe List.empty
  }

}
