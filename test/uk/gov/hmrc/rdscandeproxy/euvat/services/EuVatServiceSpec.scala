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

import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.rdscandeproxy.euvat.models.requests.ApplicationRequest
import uk.gov.hmrc.rdscandeproxy.euvat.models.responses.ApplicationResponse
import uk.gov.hmrc.rdscandeproxy.euvat.repositories.EuVatCandeRepository

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class EuVatServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar with IntegrationPatience:

  implicit val ec: ExecutionContext = global
//  private val mockConnector = mock[EuVatCandeRepository]
//  private val service = new EuVatService(mockConnector)

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

  "EuVatService" should:
    val repo = mock[EuVatCandeRepository]
    val service = new EuVatService(repo)

    val req = mock[ApplicationRequest]
    "succeed" when:
      "add application to the database" in:
        val expected = ApplicationResponse(1, "APP-123", 10)

        when(repo.addApplication(req)).thenReturn(Future.successful(expected))

        val result = service.addApplication(req)

        result shouldBe Future.successful(expected)
        verify(repo).addApplication(req)

//      "retrieving traders known facts" in:
//        when(mockConnector.getTraderByVrn(any()))
//          .thenReturn(Future.successful(Some(KnownFactsResponse)))
//        val result = service.retrieveTraderByVrn("123").futureValue
//        result shouldBe Some(KnownFactsResponse)

//    "fail" when:
//      "while saving to database" in:
//        when(mockConnector.addApplication(any()))
//          .thenReturn(Future.failed(new Exception("bang")))
//
//        val result = intercept[Exception](service.addApplication(req).futureValue)
//        result.getMessage should include("bang")
