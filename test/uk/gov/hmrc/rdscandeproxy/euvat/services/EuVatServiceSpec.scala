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

package uk.gov.hmrc.rdscandeproxy.euvat.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdscandeproxy.euvat.models.requests.LatestApplicationRequest
import uk.gov.hmrc.rdscandeproxy.euvat.models.responses.LatestApplicationResponse
import uk.gov.hmrc.rdscandeproxy.euvat.repositories.EuVatCandeRepository

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class EuVatServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar with IntegrationPatience:

  implicit val ec: ExecutionContext = global
  private val mockConnector = mock[EuVatCandeRepository]
  private val service = new EuVatService(mockConnector)

  val sampleRequest: LatestApplicationRequest = LatestApplicationRequest(
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

  val sampleResponse: LatestApplicationResponse = LatestApplicationResponse(
    applications     = List.empty,
    totalApplication = 0
  )

  "EuVatService" should:
    "succeed" when:
      "retrieving latest applications" in:
        when(mockConnector.getLatestApplications(any()))
          .thenReturn(Future.successful(sampleResponse))
        val result = service.getLatestApplications(sampleRequest).futureValue
        result shouldBe sampleResponse

    "fail" when:
      "retrieving latest applications" in:
        when(mockConnector.getLatestApplications(any()))
          .thenReturn(Future.failed(new Exception("bang")))
        val result = intercept[Exception](service.getLatestApplications(sampleRequest).futureValue)
        result.getMessage should include("bang")
