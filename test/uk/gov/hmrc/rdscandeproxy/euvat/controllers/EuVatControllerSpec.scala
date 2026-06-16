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
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.rdscandeproxy.euvat.base.SpecBase
import uk.gov.hmrc.rdscandeproxy.euvat.models.requests.LatestApplicationRequest
import uk.gov.hmrc.rdscandeproxy.euvat.models.responses.{LatestApplicationResponse, TradersKnownFacts}
import uk.gov.hmrc.rdscandeproxy.euvat.services.EuVatService

import java.time.LocalDateTime
import scala.concurrent.Future

class EuVatControllerSpec extends SpecBase with MockitoSugar {
  "EuVatController" - {

    "retrieveTraderKnownFacts" - {
      "return 200 and a successful response when DB returns records" in new SetUp {
        when(mockEuVatService.retrieveTraderByVrn(any[String]))
          .thenReturn(Future.successful(Some(KnownFactsResponse)))
        val result: Future[Result] = controller.retrieveTraderByVrn()(fakeRequest)

        status(result)        shouldBe OK
        contentType(result)   shouldBe Some("application/json")
        contentAsJson(result) shouldBe Json.toJson(KnownFactsResponse)
      }

      "return 200 and an empty records when no data returned from DB" in new SetUp {
        when(mockEuVatService.retrieveTraderByVrn(any[String]))
          .thenReturn(Future.successful(Some(emptyKnownFactsResponse)))
        val result: Future[Result] = controller.retrieveTraderByVrn()(fakeRequest)

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(emptyKnownFactsResponse)
      }

      "return 500 and log error when DB call fails" in new SetUp {
        val exception = new RuntimeException("DB error")
        when(mockEuVatService.retrieveTraderByVrn(any[String]))
          .thenReturn(Future.failed(exception))
        val result: Future[Result] = controller.retrieveTraderByVrn()(fakeRequest)

        status(result)        shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) should include("Failed to retrieve traders known facts")
      }
    }

    "getLatestApplications" - {
      "return 200 with JSON when service returns latest applications" in new SetUp {
        when(mockEuVatService.getLatestApplications(any()))
          .thenReturn(Future.successful(sampleResponse))

        val result: Future[Result] = controller.getLatestApplications()(
          fakeRequest.withMethod("POST").withJsonBody(Json.toJson(sampleRequest))
        )

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(sampleResponse)
      }

      "return 400 when request body is invalid" in new SetUp {
        val result: Future[Result] = controller.getLatestApplications()(
          fakeRequest.withMethod("POST").withJsonBody(Json.obj("invalid" -> "body"))
        )

        status(result) shouldBe BAD_REQUEST
      }

      "return 500 when service throws exception" in new SetUp {
        when(mockEuVatService.getLatestApplications(any()))
          .thenReturn(Future.failed(new RuntimeException("DB error")))

        val result: Future[Result] = controller.getLatestApplications()(
          fakeRequest.withMethod("POST").withJsonBody(Json.toJson(sampleRequest))
        )

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

  }

  private class SetUp {
    val mockEuVatService: EuVatService = mock[EuVatService]

    val emptyKnownFactsResponse: TradersKnownFacts =
      TradersKnownFacts(0, "", "", "", "", "", "", "", "", LocalDateTime.MIN, LocalDateTime.MIN, "", 0)

    val KnownFactsResponse: TradersKnownFacts =
      TradersKnownFacts(
        123456789,
        "TestData",
        "Line 1",
        "Line 2",
        "Line 3",
        "Line 4",
        "Line 5",
        "NE3 9TG",
        "7020",
        LocalDateTime.of(2025, 1, 11, 10, 38),
        LocalDateTime.of(2026, 1, 11, 10, 38),
        "N",
        1
      )

    val controller = new EuVatController(fakeAuthAction, mockEuVatService, cc)

    val sampleRequest = LatestApplicationRequest(
      applicantVatRegNumber = "123456789",
      refundingCountry      = "LV",
      startDate             = LocalDateTime.of(2025, 2, 1, 0, 0),
      endDate               = LocalDateTime.of(2025, 5, 31, 0, 0),
      representativeId      = "rep123",
      maxNumber             = 10,
      orderBy               = None,
      sortOrder             = None,
      startAt               = None
    )

    val sampleResponse = LatestApplicationResponse(
      applications     = List.empty,
      totalApplication = 0
    )

  }
}
