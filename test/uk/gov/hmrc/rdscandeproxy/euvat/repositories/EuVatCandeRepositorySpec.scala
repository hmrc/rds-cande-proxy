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
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.db.Database
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.rdscandeproxy.euvat.models.requests.ApplicationRequest
import uk.gov.hmrc.rdscandeproxy.euvat.models.responses.ApplicationResponse

import java.sql.{CallableStatement, Connection}
import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class EuVatCandeRepositorySpec extends AnyFlatSpec with Matchers with BeforeAndAfter {

  var db: Database = _
  var repository: EuVatCandeRepository = _
  var mockConnection: java.sql.Connection = _
  var mockCallableStatement: CallableStatement = _

  before {
    // Mocking the database connection and callable statement
    db                    = mock(classOf[Database])
    mockConnection        = mock(classOf[Connection])
    mockCallableStatement = mock(classOf[CallableStatement])

    // When db.withConnection is called, it should invoke the passed-in function and return the result
    when(db.withConnection(any())).thenAnswer { invocation =>
      val func = invocation.getArgument(0, classOf[Connection => Any])
      func(mockConnection) // Return the result of the lambda function passed to withConnection
    }

    // When prepareCall is invoked on the connection, return the mocked callable statement
    when(mockConnection.prepareCall(any())).thenReturn(mockCallableStatement)

    // Initialize the repository with the mocked db connection
    repository = new EuVatCandeRepository(db)
  }

  "addApplication" should "return saved application response" in {
    val appRequest: ApplicationRequest = ApplicationRequest(
//      applicantVatRegNumber         = "123456789",
      refundingCountryCode          = Some("FR"),
      periodStartDate               = Some(LocalDateTime.of(2025, 1, 1, 0, 0, 0)),
      periodEndDate                 = Some(LocalDateTime.of(2025, 3, 31, 23, 59, 59)),
      applicantEmailAddress         = Some("test@email.com"),
      applicantTelephoneNumber      = Some("0123456789"),
      applicationLanguage           = Some("EN"),
      businessActivityCode1         = Some("7090"),
      businessActivityCode2         = Some("8903"),
      businessActivityCode3         = None,
      representativeId              = None,
      representativeCountryCode     = None,
      representativeEmailAddress    = None,
      representativeIdType          = None,
      representativeTelephoneNumber = None,
      bankAccountOwnerName          = None,
      bankAccountOwnerType          = None,
      iBanCode                      = None,
      bicCode                       = None,
      bankAccountCurrencyCode       = None
    )

    val applicationResponse: ApplicationResponse = ApplicationResponse(1, "GB123456", 1)

    // Mock output parameters
    when(mockCallableStatement.getInt("p_application_id")).thenReturn(1)
    when(mockCallableStatement.getString("p_application_number")).thenReturn("GB123456")
    when(mockCallableStatement.getInt("p_update_seq_number")).thenReturn(1)

    val result = await(repository.addApplication(appRequest, "123456789"))

    result shouldBe applicationResponse
  }

}
