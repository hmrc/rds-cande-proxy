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

package uk.gov.hmrc.rdscandeproxy.euvat.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdscandeproxy.euvat.base.SpecBase
import uk.gov.hmrc.rdscandeproxy.euvat.models.requests.ApplicationRequest
import uk.gov.hmrc.rdscandeproxy.euvat.models.responses.ApplicationResponse
import uk.gov.hmrc.rdscandeproxy.euvat.services.EuVatService

import java.time.LocalDateTime
import scala.concurrent.Future

class EuVatControllerSpec extends SpecBase {
  "EuVatController" - {

    "add application" - {
      "return 200 and a successful response when DB returns records" in new SetUp {
        when(mockEuVatService.addApplication(any(), any()))
          .thenReturn(Future.successful(applicationResponse))

        val result: Future[Result] = controller.addApplication()(
          fakeRequest.withMethod("POST").withJsonBody(Json.toJson(appRequest))
        )

        status(result)        shouldBe OK
        contentType(result)   shouldBe Some("application/json")
        contentAsJson(result) shouldBe Json.toJson(applicationResponse)

      }

      "return 400 when request body is missing" in new SetUp {
        val result: Future[Result] = controller.addApplication()(
          fakeRequest.withMethod("POST")
        )

        status(result)          shouldBe BAD_REQUEST
        contentAsString(result) shouldBe "Invalid request body"
      }

      "return 500 and log error when DB call fails" in new SetUp {
        val exception = new RuntimeException("DB error")
        when(mockEuVatService.addApplication(any(), any()))
          .thenReturn(Future.failed(exception))
        val result: Future[Result] = controller.addApplication()(
          fakeRequest.withMethod("POST").withJsonBody(Json.toJson(appRequest))
        )

        status(result)        shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) should include("Failed to create refund application")
      }
    }

  }

  private class SetUp {
    val mockEuVatService: EuVatService = mock[EuVatService]

    val appRequest: ApplicationRequest = ApplicationRequest(
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

    val applicationResponse: ApplicationResponse = ApplicationResponse(1, "GB9999991", 1)

    val controller = new EuVatController(fakeAuthAction, mockEuVatService, cc)

  }
}
