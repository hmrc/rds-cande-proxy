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
import uk.gov.hmrc.rdscandeproxy.euvat.models.{Attachment, PurchaseImport}
import uk.gov.hmrc.rdscandeproxy.euvat.models.requests.*
import uk.gov.hmrc.rdscandeproxy.euvat.models.responses.*
import uk.gov.hmrc.rdscandeproxy.euvat.repositories.EuVatCandeRepository

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class EuVatServiceSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar with IntegrationPatience {

  implicit val ec: ExecutionContext = global
  private val mockConnector = mock[EuVatCandeRepository]
  private val service = new EuVatService(mockConnector)

  "EuVatService.getLatestApplications" should {
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
  }

  "EuVatService.addApplication" should {
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

    val applicationResponse: ApplicationResponse = ApplicationResponse(1, "GB", 1)

    "succeed" when:
      "add application to the database" in:
        when(mockConnector.addApplication(any(), any()))
          .thenReturn(Future.successful(applicationResponse))
        val result = service.addApplication(appRequest, "123456789").futureValue
        result shouldBe applicationResponse

    "fail" when:
      "while saving to database" in:
        when(mockConnector.addApplication(any(), any()))
          .thenReturn(Future.failed(new Exception("bang")))

        val result = intercept[Exception](service.addApplication(appRequest, "123456789").futureValue)
        result.getMessage should include("bang")
  }

  "EuVatService.addPurchase" should {
    val purchaseRequest: AddPurchaseRequest = AddPurchaseRequest(
      applicationId              = 123456,
      goodsDescriptionCode       = "1",
      goodsDescriptionText       = Some("Fuel"),
      goodsDescriptionSubCode    = None,
      simplifiedInvoiceIndicator = None,
      supplierName               = None,
      supplierAddress1           = None,
      supplierAddress2           = None,
      supplierAddress3           = None,
      supplierVatRegNumber       = None,
      supplierTaxIdentifier      = None,
      invoiceDate                = None,
      invoiceNumber              = None,
      currencyCode               = None,
      taxableAmount              = None,
      vatAmount                  = None,
      deductibleVatAmount        = None,
      updateSequenceNumber       = 1
    )

    val purchaseResponse: AddPurchaseResponse = AddPurchaseResponse(itemNumber = 4, updateSequenceNumber = 1)

    "succeed" when:
      "adding a purchase to the database" in:
        when(mockConnector.addPurchase(any()))
          .thenReturn(Future.successful(purchaseResponse))
        val result = service.addPurchase(purchaseRequest).futureValue
        result shouldBe purchaseResponse

    "fail" when:
      "saving to the database" in:
        when(mockConnector.addPurchase(any()))
          .thenReturn(Future.failed(new Exception("bang")))
        val result = intercept[Exception](service.addPurchase(purchaseRequest).futureValue)
        result.getMessage should include("bang")
  }

  "EuVatService.addImport" should {
    val importRequest: AddImportRequest = AddImportRequest(
      applicationId        = 123456,
      goodsDescriptionCode = "3",
      goodsDescriptionText = Some("Electronics"),
      updateSequenceNumber = 1
    )

    val importResponse: AddImportResponse = AddImportResponse(itemNumber = 4, updateSequenceNumber = 1)

    "succeed" when:
      "adding an import to the database" in:
        when(mockConnector.addImportation(any())).thenReturn(Future.successful(importResponse))
        val result = service.addImport(importRequest).futureValue
        result shouldBe importResponse

    "fail" when:
      "saving to the database" in:
        when(mockConnector.addImportation(any())).thenReturn(Future.failed(new Exception("bang")))
        val result = intercept[Exception](service.addImport(importRequest).futureValue)
        result.getMessage should include("bang")
  }

  "EuVatService.getPurchaseDetails" should {
    val detailsRequest: GetPurchaseDetailsRequest = GetPurchaseDetailsRequest(applicationId = 123456, itemNumber = 4)

    val detailsResponse: GetPurchaseDetailsResponse = GetPurchaseDetailsResponse(
      goodsDescriptionCode       = "1",
      goodsDescriptionSubCode    = None,
      goodsDescriptionText       = Some("Fuel"),
      simplifiedInvoiceIndicator = None,
      supplierName               = None,
      supplierAddressLine1       = None,
      supplierAddressLine2       = None,
      supplierAddressLine3       = None,
      supplierVatNumber          = None,
      supplierTaxIdentifier      = None,
      invoiceDate                = None,
      invoiceNumber              = None,
      currencyCode               = None,
      taxableAmount              = None,
      vatAmount                  = None,
      deductibleVatAmount        = None,
      updateSequenceNumber       = 1
    )

    "succeed" when:
      "retrieving a purchase record" in:
        when(mockConnector.getPurchaseDetails(any()))
          .thenReturn(Future.successful(Some(detailsResponse)))
        val result = service.getPurchaseDetails(detailsRequest).futureValue
        result shouldBe Some(detailsResponse)

      "no purchase record exists" in:
        when(mockConnector.getPurchaseDetails(any()))
          .thenReturn(Future.successful(None))
        val result = service.getPurchaseDetails(detailsRequest).futureValue
        result shouldBe None

    "fail" when:
      "reading from the database" in:
        when(mockConnector.getPurchaseDetails(any()))
          .thenReturn(Future.failed(new Exception("bang")))
        val result = intercept[Exception](service.getPurchaseDetails(detailsRequest).futureValue)
        result.getMessage should include("bang")
  }

  "EuVatService.getImportationDetails" should {
    val importationRequest: GetImportationDetailsRequest = GetImportationDetailsRequest(applicationId = 404, itemNumber = 4)

    val importationResponse: GetImportationDetailsResponse = GetImportationDetailsResponse(
      applicationId           = 404,
      itemNumber              = 4,
      issuingDate             = None,
      supplierName            = None,
      supplierAddressLine1    = None,
      supplierAddressLine2    = None,
      supplierAddressLine3    = None,
      supplierCountryCode     = None,
      goodsDescriptionCode    = "10",
      goodsDescriptionSubCode = None,
      goodsDescriptionText    = None,
      hasSadReferenceNumber   = None,
      sadReferenceNumber      = None,
      referenceInformation    = None,
      currencyCode            = None,
      taxableAmount           = None,
      vatAmount               = None,
      deductibleVatAmount     = None,
      updateSequenceNumber    = 1,
      periodStartDate         = LocalDateTime.of(2026, 1, 1, 0, 0)
    )

    "succeed" when:
      "retrieving an importation record" in:
        when(mockConnector.getImportationDetails(any()))
          .thenReturn(Future.successful(Some(importationResponse)))
        val result = service.getImportationDetails(importationRequest).futureValue
        result shouldBe Some(importationResponse)

      "no importation record exists" in:
        when(mockConnector.getImportationDetails(any()))
          .thenReturn(Future.successful(None))
        val result = service.getImportationDetails(importationRequest).futureValue
        result shouldBe None

    "fail" when:
      "reading from the database" in:
        when(mockConnector.getImportationDetails(any()))
          .thenReturn(Future.failed(new Exception("bang")))
        val result = intercept[Exception](service.getImportationDetails(importationRequest).futureValue)
        result.getMessage should include("bang")
  }

  "EuVatService.getSupplierVrnCount" should {
    val sampleRequest: SupplierVrnCountRequest = SupplierVrnCountRequest(
      applicationId = 133,
      itemNumber    = 4,
      vatNumber     = "500000881",
      invoiceNumber = "a444"
    )
    val sampleResponse: SupplierVrnCountResponse = SupplierVrnCountResponse(duplicateCount = 1)

    "succeed" when:
      "retrieving supplier VRN count" in:
        when(mockConnector.getSupplierVrnCount(any()))
          .thenReturn(Future.successful(sampleResponse))
        val result = service.getSupplierVrnCount(sampleRequest).futureValue
        result shouldBe sampleResponse

    "fail" when:
      "retrieving supplier VRN count" in:
        when(mockConnector.getSupplierVrnCount(any()))
          .thenReturn(Future.failed(new Exception("bang")))
        val result = intercept[Exception](service.getSupplierVrnCount(sampleRequest).futureValue)
        result.getMessage should include("bang")
  }

  "EuVatService.getSupplierTaxIdentifierDuplicateCount" should {
    val req = SupplierTaxIdentifierCountRequest(
      applicationId = 133,
      itemNumber    = 4,
      taxIdentifier = "500000881",
      invoiceNumber = "a444"
    )

    "succeed" in {
      when(mockConnector.getSupplierTaxIdentifierDuplicateCount(any())).thenReturn(Future.successful(4))
      val result = service.getSupplierTaxIdentifierDuplicateCount(req).futureValue
      result shouldBe 4
    }

    "fail" in {
      when(mockConnector.getSupplierTaxIdentifierDuplicateCount(any())).thenReturn(Future.failed(new Exception("bang")))
      val result = intercept[Exception](service.getSupplierTaxIdentifierDuplicateCount(req).futureValue)
      result.getMessage should include("bang")
    }
  }

  "EuVatService.updatePurchaseDetails" should {
    val req = UpdatePurchaseDetailsRequest(
      applicationId              = 404,
      itemNumber                 = 4,
      goodsDescriptionCode       = "10",
      goodsDescriptionSubCode    = Some("10.4.1"),
      goodsDescriptionText       = Some("office stationery and consumables"),
      simplifiedInvoiceIndicator = Some("false"),
      supplierName               = Some("Finnish International"),
      supplierAddress1           = Some("356 High Street"),
      supplierAddress2           = Some("Rochdale"),
      supplierAddress3           = Some("England"),
      supplierVatRegNumber       = Some("500000881"),
      supplierTaxIdentifier      = Some(""),
      invoiceDate                = Some(LocalDateTime.of(2026, 5, 14, 0, 0)),
      invoiceNumber              = Some("a444"),
      currencyCode               = Some("EUR"),
      taxableAmount              = Some(BigDecimal(1000)),
      vatAmount                  = Some(BigDecimal(99)),
      deductibleVatAmount        = Some(BigDecimal(40)),
      updateSequenceNumber       = 1
    )

    "succeed" in {
      when(mockConnector.updatePurchaseDetails(any())).thenReturn(Future.successful(2))
      val result = service.updatePurchaseDetails(req).futureValue
      result shouldBe UpdatePurchaseDetailsResponse(2)
    }

    "fail" in {
      when(mockConnector.updatePurchaseDetails(any())).thenReturn(Future.failed(new Exception("bang")))
      val result = intercept[Exception](service.updatePurchaseDetails(req).futureValue)
      result.getMessage should include("bang")
    }
  }

  "EuVatService.getPurchaseImportList" should {
    val req = PurchaseImportListRequest(applicationId = 133)
    val purchaseImport = PurchaseImport(123, "2", Some("2.3"), "EU", BigDecimal(300), BigDecimal(200), BigDecimal(100), "P")

    "succeed" in {
      when(mockConnector.getPurchaseImportList(any())).thenReturn(Future.successful(PurchaseImportListResponse(List(purchaseImport), 1, 100)))
      val result = service.getPurchaseImportList(req).futureValue
      result shouldBe PurchaseImportListResponse(List(purchaseImport), 1, 100)
    }

    "fail" in {
      when(mockConnector.getPurchaseImportList(any())).thenReturn(Future.failed(new Exception("bang")))
      val result = intercept[Exception](service.getPurchaseImportList(req).futureValue)
      result.getMessage should include("bang")
    }
  }

  "EuVatService.deleteApplication" should {
    val req = DeleteApplicationRequest(applicationId = 999, updateSequenceNumber = 4)

    "succeed" in {
      when(mockConnector.deleteApplication(any())).thenReturn(Future.successful(()))
      service.deleteApplication(req).futureValue
    }

    "fail" in {
      when(mockConnector.deleteApplication(any())).thenReturn(Future.failed(new Exception("bang")))
      val result = intercept[Exception](service.deleteApplication(req).futureValue)
      result.getMessage should include("bang")
    }
  }

  "EuVatService.updateApplicationDetails" should {
    val req = UpdateApplicationDetailsRequest(
      applicationId              = 133,
      applicationLanguage        = "en",
      refundingCountry           = "LV",
      periodStartDate            = LocalDateTime.of(2011, 6, 1, 0, 0),
      periodEndDate              = LocalDateTime.of(2011, 10, 31, 23, 59, 59),
      applicantEmailAddress      = "test@hotmail.com",
      applicantPhoneNumber       = Some("01952233299"),
      representativeCountry      = None,
      representativeEmailAddress = None,
      representativePhoneNumber  = None,
      bankAccountOwnerName       = None,
      bankAccountOwnerType       = None,
      ibanCode                   = None,
      bicCode                    = None,
      bankAccountCurrencyCode    = None,
      businessActivityCode2      = None,
      businessActivityCode3      = None,
      cipherText                 = None,
      encryptionStatus           = None,
      updateSequenceNumber       = 30
    )

    "succeed" in {
      when(mockConnector.updateApplicationDetails(any())).thenReturn(Future.successful(32))
      val result = service.updateApplicationDetails(req).futureValue
      result shouldBe UpdateApplicationDetailsResponse(32)
    }

    "fail" in {
      when(mockConnector.updateApplicationDetails(any())).thenReturn(Future.failed(new Exception("bang")))
      val result = intercept[Exception](service.updateApplicationDetails(req).futureValue)
      result.getMessage should include("bang")
    }
  }

  "EuVatService.updateImportationDetails" should {
    val req = UpdateImportDetailsRequest(
      applicationId           = 404,
      itemNumber              = 4,
      goodsDescriptionCode    = "10",
      goodsDescriptionSubCode = Some("10.4.1"),
      goodsDescriptionText    = Some("office stationery and consumables"),
      hasSadReferenceNumber   = Some("true"),
      sadReferenceNumber      = Some("SAD123456789"),
      referenceInformation    = Some("Import declaration reference"),
      supplierName            = Some("Finnish International"),
      supplierAddressLine1    = Some("356 High Street"),
      supplierAddressLine2    = Some("Rochdale"),
      supplierAddressLine3    = Some("England"),
      supplierCountryCode     = Some("GB"),
      issuingDate             = Some(LocalDateTime.of(2026, 5, 14, 0, 0)),
      currencyCode            = Some("EUR"),
      taxableAmount           = Some(BigDecimal(1000)),
      vatAmount               = Some(BigDecimal(99)),
      deductibleVatAmount     = Some(BigDecimal(40)),
      updateSequenceNumber    = 1
    )

    "succeed" in {
      when(mockConnector.updateImportationDetails(any())).thenReturn(Future.successful(2))
      val result = service.updateImportationDetails(req).futureValue
      result shouldBe UpdateImportDetailsResponse(2)
    }

    "fail" in {
      when(mockConnector.updateImportationDetails(any())).thenReturn(Future.failed(new Exception("bang")))
      val result = intercept[Exception](service.updateImportationDetails(req).futureValue)
      result.getMessage should include("bang")
    }
  }
  "EuVatService.deletePurchase" should {
    val req = DeletePurchaseRequest(applicationId = 404, itemNumber = 4, updateSequenceNumber = 1)

    "succeed" in {
      when(mockConnector.deletePurchase(any())).thenReturn(Future.successful(2))
      val result = service.deletePurchase(req).futureValue
      result shouldBe DeletePurchaseResponse(2)
    }

    "fail" in {
      when(mockConnector.deletePurchase(any())).thenReturn(Future.failed(new Exception("bang")))
      val result = intercept[Exception](service.deletePurchase(req).futureValue)
      result.getMessage should include("bang")
    }
  }

  "EuVatService.getSadReferenceCount" should {
    val sampleRequest: SadReferenceCountRequest = SadReferenceCountRequest(
      applicationId      = 133,
      itemNumber         = 4,
      sadReferenceNumber = "500000881"
    )
    val sampleResponse: SadReferenceCountResponse = SadReferenceCountResponse(duplicateCount = 1)

    "succeed" when:
      "retrieving SAD reference count" in:
        when(mockConnector.getSadReferenceCount(any()))
          .thenReturn(Future.successful(sampleResponse))
        val result = service.getSadReferenceCount(sampleRequest).futureValue
        result shouldBe sampleResponse

    "fail" when:
      "retrieving SAD reference count" in:
        when(mockConnector.getSadReferenceCount(any()))
          .thenReturn(Future.failed(new Exception("bang")))
        val result = intercept[Exception](service.getSadReferenceCount(sampleRequest).futureValue)
        result.getMessage should include("bang")
  }

  "EuVatService.deleteImport" should {
    val req = DeleteImportRequest(applicationId = 404L, itemNumber = 4, updateSequenceNumber = 1)

    "succeed" in {
      when(mockConnector.deleteImport(any())).thenReturn(Future.successful(2))
      val result = service.deleteImport(req).futureValue
      result shouldBe DeleteImportResponse(2)
    }

    "fail" in {
      when(mockConnector.deleteImport(any())).thenReturn(Future.failed(new Exception("bang")))
      val result = intercept[Exception](service.deleteImport(req).futureValue)
      result.getMessage should include("bang")
    }
  }

  "EuVatService.getReferenceInformationCount" should {
    val request = ReferenceInfoCountRequest(applicationId = 123, itemNumber = 1)

    "return successful response when connector call succeeds" in {
      val response = ReferenceInfoCountResponse(1)

      when(mockConnector.getReferenceInformationCount(request))
        .thenReturn(Future.successful(response))

      service.getReferenceInformationCount(request).futureValue shouldBe response
    }

    "fail when connector call fails" in {
      val errorMessage = "Connector error"

      when(mockConnector.getReferenceInformationCount(any())).thenReturn(Future.failed(new Exception(errorMessage)))

      val result = service.getReferenceInformationCount(request).failed.futureValue

      result.getMessage should include(errorMessage)
    }
  }

  "EuVatService.getApplicationDetails" should {
    val applicationId = 133L
    val response = ApplicationDetailsResponse(List.empty)

    "return application details from the repository" in:
      when(mockConnector.getApplicationDetails(applicationId))
        .thenReturn(Future.successful(response))

      service.getApplicationDetails(applicationId).futureValue shouldBe response

    "propagate repository failures" in:
      when(mockConnector.getApplicationDetails(applicationId))
        .thenReturn(Future.failed(new Exception("bang")))

      val result = intercept[Exception](service.getApplicationDetails(applicationId).futureValue)
      result.getMessage should include("bang")
  }

  "EuVatService.getAttachmentDetails" should {
    val getAttachmentRequest: GetAttachmentDetailsRequest = GetAttachmentDetailsRequest(applicationId = 404L, attachmentNumber = 4)

    val getAttachmentResponse: GetAttachmentDetailsResponse = GetAttachmentDetailsResponse(
      attachmentNumber     = 4,
      fileName             = Some("test.pdf"),
      fileType             = Some("pdf"),
      fileDescription      = Some("pdf file attached"),
      fileSize             = Some(42),
      updateSequenceNumber = 10
    )

    "succeed" when:
      "retrieving an attachment record" in:
        when(mockConnector.getAttachmentDetails(any()))
          .thenReturn(Future.successful(Some(getAttachmentResponse)))
        val result = service.getAttachmentDetails(getAttachmentRequest).futureValue
        result shouldBe Some(getAttachmentResponse)

      "no attachment record exists" in:
        when(mockConnector.getAttachmentDetails(any()))
          .thenReturn(Future.successful(None))
        val result = service.getAttachmentDetails(getAttachmentRequest).futureValue
        result shouldBe None

    "fail" when:
      "reading from the database" in:
        when(mockConnector.getAttachmentDetails(any()))
          .thenReturn(Future.failed(new Exception("bang")))
        val result = intercept[Exception](service.getAttachmentDetails(getAttachmentRequest).futureValue)
        result.getMessage should include("bang")
  }

  "EuVatService.getAllAttachments" should {
    val req = GetAllAttachmentsRequest(applicationId = 104)
    val response = GetAllAttachmentsResponse(
      attachments      = List(Attachment(123, Some("file1"), Some("pdf"), Some("test file"), Some(1825))),
      totalAttachments = 1
    )

    "succeed" in {
      when(mockConnector.getAllAttachments(any())).thenReturn(Future.successful(response))
      service.getAllAttachments(req).futureValue shouldBe response
    }

    "fail" in {
      when(mockConnector.getAllAttachments(any())).thenReturn(Future.failed(new Exception("bang")))
      val result = intercept[Exception](service.getAllAttachments(req).futureValue)
      result.getMessage should include("bang")
    }
  }

  "EuVatService.getFullAttachmentDetails" should {
    val getFullAttachmentRequest: GetFullAttachmentDetailsRequest = GetFullAttachmentDetailsRequest(applicationId = 404L, attachmentNumber = 4)

    val getFullAttachmentResponse: GetFullAttachmentDetailsResponse = GetFullAttachmentDetailsResponse(
      attachmentNumber     = 4,
      fileName             = Some("test.pdf"),
      fileType             = Some("pdf"),
      fileDescription      = Some("pdf file attached"),
      fileSize             = Some(42),
      fileContent          = Some("dGVzdCBmaWxlIGNvbnRlbnQ="),
      updateSequenceNumber = 10
    )

    "succeed" when:
      "retrieving a full attachment record" in:
        when(mockConnector.getFullAttachmentDetails(any()))
          .thenReturn(Future.successful(Some(getFullAttachmentResponse)))
        val result = service.getFullAttachmentDetails(getFullAttachmentRequest).futureValue
        result shouldBe Some(getFullAttachmentResponse)

      "no attachment record exists" in:
        when(mockConnector.getFullAttachmentDetails(any()))
          .thenReturn(Future.successful(None))
        val result = service.getFullAttachmentDetails(getFullAttachmentRequest).futureValue
        result shouldBe None

    "fail" when:
      "reading from the database" in:
        when(mockConnector.getFullAttachmentDetails(any()))
          .thenReturn(Future.failed(new Exception("bang")))
        val result = intercept[Exception](service.getFullAttachmentDetails(getFullAttachmentRequest).futureValue)
        result.getMessage should include("bang")
  }

  "EuVatService.deleteAttachment" should {
    val req = DeleteAttachmentRequest(applicationId = 404L, attachmentNumber = 4, updateSequenceNumber = 1)

    "succeed" in {
      when(mockConnector.deleteAttachment(any())).thenReturn(Future.successful(DeleteAttachmentResponse(2)))
      val result = service.deleteAttachment(req).futureValue
      result shouldBe DeleteAttachmentResponse(2)
    }

    "fail" in {
      when(mockConnector.deleteAttachment(any())).thenReturn(Future.failed(new Exception("bang")))
      val result = intercept[Exception](service.deleteAttachment(req).futureValue)
      result.getMessage should include("bang")
    }
  }

  "EuVatService.addAttachment" should {

    val attachmentRequest = AddAttachmentRequest(
      applicationId        = 123456L,
      fileName             = "invoice.pdf",
      fileType             = "application/pdf",
      fileDescription      = Some("Supporting invoice"),
      fileSize             = 5,
      fileContent          = Array[Byte](1, 2, 3, 4, 5),
      updateSequenceNumber = 1
    )

    val attachmentResponse = AddAttachmentResponse(
      attachmentNumber     = 4,
      updateSequenceNumber = 2
    )

    "succeed" when:
      "adding an attachment to the database" in:
        when(mockConnector.addAttachment(any()))
          .thenReturn(Future.successful(attachmentResponse))

        val result = service.addAttachment(attachmentRequest).futureValue

        result shouldBe attachmentResponse

    "fail" when:
      "saving the attachment to the database" in:
        when(mockConnector.addAttachment(any()))
          .thenReturn(Future.failed(new Exception("bang")))

        val result =
          intercept[Exception](service.addAttachment(attachmentRequest).futureValue)

        result.getMessage should include("bang")
  }

  "EuVatService.getSubmissionDateTime" should {
    val applicationId = 133L
    val response = SubmissionDateTimeResponse(LocalDateTime.of(2026, 11, 27, 13, 43))

    "return the submission datetime from the repository" in {
      when(mockConnector.getSubmissionDateTime(applicationId)).thenReturn(Future.successful(response))

      service.getSubmissionDateTime(applicationId).futureValue shouldBe response
    }

    "propagate repository failures" in {
      when(mockConnector.getSubmissionDateTime(applicationId))
        .thenReturn(Future.failed(new Exception("DB error")))

      service.getSubmissionDateTime(applicationId).failed.futureValue.getMessage shouldBe "DB error"
    }
  }

}
