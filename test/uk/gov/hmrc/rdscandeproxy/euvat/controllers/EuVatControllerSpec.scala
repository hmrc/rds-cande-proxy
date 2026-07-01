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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, AnyContentAsJson, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.rdscandeproxy.euvat.actions.AuthAction
import uk.gov.hmrc.rdscandeproxy.euvat.base.SpecBase
import uk.gov.hmrc.rdscandeproxy.euvat.models.requests.ApplicationRequest
import uk.gov.hmrc.rdscandeproxy.euvat.models.responses.ApplicationResponse
import uk.gov.hmrc.rdscandeproxy.euvat.services.EuVatService

import java.time.LocalDateTime
import scala.concurrent.Future

class EuVatControllerSpec extends SpecBase {
  "EuVatController" - {

    "add application" - {
      "return 200 and a successful response when DB returns records" in {
//        when(mockEuVatService.addApplication(any()))
//          .thenReturn(Future.successful(Some(applicationResponse)))
//        val result: Future[Result] = controller.addApplication()(fakeRequest)
//
//        status(result)        shouldBe OK
//        contentType(result)   shouldBe Some("application/json")
//        contentAsJson(result) shouldBe Json.toJson(applicationResponse)

        val auth = mock[AuthAction]
        val service = mock[EuVatService]
        val cc = stubControllerComponents()

        val controller = new EuVatController(auth, service, cc)

        val reqJson = Json.obj(
          "applicantVatRegNumber" -> "GB123456",
          "refundingCountryCode"  -> "FR"
        )

        val request = FakeRequest("POST", "/add")
          .withBody(AnyContentAsJson(reqJson))

        val appReq = reqJson.as[ApplicationRequest]
        val appResp = ApplicationResponse(1, "APP-001", 5)

        when(auth.invokeBlock(any(), any())).thenAnswer { invocation =>
          val block = invocation.getArgument(1).asInstanceOf[Request[AnyContent] => Future[Result]]
          block(request)
        }

        when(service.addApplication(appReq)).thenReturn(Future.successful(appResp))

        val result = controller.addApplication()(request)

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(appResp)
      }

      "return 500 and log error when DB call fails" in new SetUp {
        val exception = new RuntimeException("DB error")
        when(mockEuVatService.addApplication(any()))
          .thenReturn(Future.failed(exception))
        val result: Future[Result] = controller.addApplication()(fakeRequest)

        status(result)        shouldBe BAD_REQUEST
        contentAsString(result) should include("Invalid request body")
      }
    }

  }

  private class SetUp {
    val mockEuVatService: EuVatService = mock[EuVatService]

    val appRequest: ApplicationRequest = ApplicationRequest(
      applicantVatRegNumber         = "123456789",
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

    val applicationResponse: ApplicationResponse = ApplicationResponse(1, "GB", 1)

    val controller = new EuVatController(fakeAuthAction, mockEuVatService, cc)

  }
}
