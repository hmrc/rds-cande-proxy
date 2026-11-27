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

package uk.gov.hmrc.rdscandeproxy.euvat.models.responses

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{Json, JsonObject}

import java.time.LocalDateTime

class GetApplicationForSubmissionResponseSpec extends AnyWordSpec with Matchers {

  private val date = LocalDateTime.of(2026, 5, 14, 0, 0)

  private val applicationDetails = SubmissionApplicationDetails(
    applicationId                = 133L,
    applicationNumber            = Some("GB123456"),
    application_version          = Some(date),
    refunding_country_code       = Some("LV"),
    period_start_date            = Some(date),
    period_end_date              = Some(date),
    application_language         = Some("en"),
    applicant_vat_reg_number     = Some("787878787"),
    applicant_email_address      = Some("test@test.com"),
    applicant_telephone_num      = Some("123456789"),
    representative_id            = Some("456456456"),
    representative_id_type       = Some("NVAT"),
    representative_country       = Some("AO"),
    representative_email_address = Some("test@test.com"),
    representative_telephone_num = Some("23457943"),
    bank_account_owner_name      = Some("Mr Adam Gill"),
    bank_account_owner_type      = Some("applicant"),
    iban_code                    = Some("GB15HBUK40127612345678"),
    bic_code                     = Some("HBUKGB4B"),
    bank_account_currency_code   = Some("EUR"),
    business_activity_code1      = Some("5757"),
    business_activity_code2      = Some("1234"),
    business_activity_code3      = Some("6789"),
    application_status           = Some("D"),
    submission_status            = None,
    message_id                   = None,
    correlation_id               = None,
    cipher_text                  = Some("badd2539694a0b3fd547b7cfcb77eefe"),
    encryption_status            = Some("E"),
    updateSeqNumber              = Some(1)
  )

  private val importationDetails = SubmissionImportationDetails(
    itemNumber              = Some(4),
    issuingDate             = Some(LocalDateTime.of(2026, 5, 14, 0, 0)),
    supplierName            = Some("Finnish International"),
    supplierAddressLine1    = Some("356 High Street"),
    supplierAddressLine2    = Some("Rochdale"),
    supplierAddressLine3    = Some("England"),
    supplierCountryCode     = Some("GB"),
    goodsDescriptionCode    = Some("10"),
    goodsDescriptionSubCode = Some("10.4.1"),
    goodsDescriptionText    = None,
    hasSadReferenceNumber   = Some("true"),
    sadReferenceNumber      = Some("SAD123456789"),
    referenceInformation    = Some("Import declaration reference"),
    currencyCode            = Some("EUR"),
    taxableAmount           = Some(BigDecimal(1000)),
    vatAmount               = Some(BigDecimal(99)),
    deductibleVatAmount     = Some(BigDecimal(40)),
    updateSequenceNumber    = Some(1),
    periodStartDate         = LocalDateTime.of(2026, 1, 1, 0, 0)
  )

  private val purchaseDetails = SubmissionPurchaseDetails(
    goodsDescriptionCode       = Some("1"),
    goodsDescriptionSubCode    = Some("1.1"),
    goodsDescriptionText       = Some("Fuel"),
    simplifiedInvoiceIndicator = Some("false"),
    supplierName               = Some("Supplier Ltd"),
    supplierAddressLine1       = Some("1 High Street"),
    supplierAddressLine2       = Some("Riga"),
    supplierAddressLine3       = None,
    supplierVatNumber          = Some("LV40003567907"),
    supplierTaxIdentifier      = None,
    invoiceDate                = Some(LocalDateTime.of(2025, 3, 15, 0, 0)),
    invoiceNumber              = Some("INV-001"),
    currencyCode               = Some("EUR"),
    taxableAmount              = Some(BigDecimal("100.50")),
    vatAmount                  = Some(BigDecimal("21.10")),
    deductibleVatAmount        = Some(BigDecimal("21.10"))
  )

  private val attachmentDetails = SubmissionAttachmentDetails(
    attachmentNumber = Some(4),
    fileName         = Some("test.pdf"),
    fileType         = Some("pdf"),
    fileDescription  = Some("pdf file attached"),
    fileSize         = Some(42),
    fileContent      = Some("dGVzdCBmaWxlIGNvbnRlbnQ=")
  )

  private val fullResponse = GetApplicationForSubmissionResponse(
    applicationDetails = List(applicationDetails),
    importationDetails = List(importationDetails),
    purchaseDetails    = List(purchaseDetails),
    attachmentDetails  = List(attachmentDetails)
  )

  "GetApplicationForSubmissionResponse JSON format" should {

    "round trip to and from JSON" in {
      Json.toJson(fullResponse).as[GetApplicationForSubmissionResponse] shouldBe fullResponse
    }

    "serialize the top level lists with the expected keys" in {
      val json = Json.toJson(fullResponse).as[JsObject]

      json.keys shouldBe Set("applicationDetails", "importationDetails", "purchaseDetails", "attachmentDetails")
    }

    "serialize empty lists" in {
      Json.toJson(GetApplicationForSubmissionResponse(List.empty, List.empty, List.empty, List.empty)) shouldBe Json.obj(
        "applicationDetails" -> Json.arr(),
        "importationDetails" -> Json.arr(),
        "purchaseDetails"    -> Json.arr(),
        "attachmentDetails"  -> Json.arr()
      )
    }
  }

  "SubmissionImportationDetails JSON format" should {

    "deserialize when only mandatory fields are present" in {
      Json.obj("itemNumber" -> 1, "goodsDescriptionCode" -> "1").as[SubmissionImportationDetails] shouldBe
        SubmissionImportationDetails(1, "1", None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)
    }

    Seq("itemNumber", "goodsDescriptionCode").foreach { field =>
      s"fail to deserialize when $field is missing" in {
        (Json.obj("itemNumber" -> 1, "goodsDescriptionCode" -> "1") - field).validate[SubmissionImportationDetails].isError shouldBe true
      }
    }
  }

  "SubmissionPurchaseDetails JSON format" should {

    "deserialize when only mandatory fields are present" in {
      Json.obj("itemNumber" -> 2, "goodsDescriptionCode" -> "2").as[SubmissionPurchaseDetails] shouldBe
        SubmissionPurchaseDetails(2, "2", None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)
    }

    Seq("itemNumber", "goodsDescriptionCode").foreach { field =>
      s"fail to deserialize when $field is missing" in {
        (Json.obj("itemNumber" -> 2, "goodsDescriptionCode" -> "2") - field).validate[SubmissionPurchaseDetails].isError shouldBe true
      }
    }
  }

  "SubmissionAttachmentDetails JSON format" should {

    "deserialize when only mandatory fields are present" in {
      Json.obj("attachmentNumber" -> 1).as[SubmissionAttachmentDetails] shouldBe
        SubmissionAttachmentDetails(1, None, None, None, None, None)
    }

    "fail to deserialize when attachmentNumber is missing" in {
      Json.obj().validate[SubmissionAttachmentDetails].isError shouldBe true
    }
  }
}
