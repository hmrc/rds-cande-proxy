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

package uk.gov.hmrc.rdscandeproxy.euvat.controllers

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.libs.json.Json
import play.api.test.Helpers.*
import uk.gov.hmrc.rdscandeproxy.euvat.base.SpecBase
import uk.gov.hmrc.rdscandeproxy.euvat.models.responses.GetApplicationCountsResponse
import uk.gov.hmrc.rdscandeproxy.euvat.services.EuVatService

import scala.concurrent.Future

class GetApplicationCountsControllerSpec extends SpecBase {

  "getApplicationCounts" - {
    "return the three application counts" in {
      val service = mock[EuVatService]
      val controller = new EuVatController(fakeAuthAction, service, cc)
      val response = GetApplicationCountsResponse(purchaseCount = 2, importCount = 3, attachmentCount = 4)
      when(service.getApplicationCounts(133L)).thenReturn(Future.successful(response))

      val result = controller.getApplicationCounts(133L)(fakeRequest.withMethod("GET"))

      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.obj(
        "purchaseCount"   -> 2,
        "importCount"     -> 3,
        "attachmentCount" -> 4
      )
    }

    "return the specified JSON error when the service fails" in {
      val service = mock[EuVatService]
      val controller = new EuVatController(fakeAuthAction, service, cc)
      when(service.getApplicationCounts(133L)).thenReturn(Future.failed(new RuntimeException("Database failure")))

      val result = controller.getApplicationCounts(133L)(fakeRequest.withMethod("GET"))

      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentAsJson(result) shouldBe Json.obj(
        "status"  -> 500,
        "error"   -> "Internal server error",
        "message" -> "Database connection issue or server issue"
      )
    }
  }
}
