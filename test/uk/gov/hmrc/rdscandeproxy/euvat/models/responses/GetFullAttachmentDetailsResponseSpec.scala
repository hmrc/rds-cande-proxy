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
import play.api.libs.json.{JsObject, Json}

class GetFullAttachmentDetailsResponseSpec extends AnyWordSpec with Matchers {

  private val fullResponse = GetFullAttachmentDetailsResponse(
    attachmentNumber     = 4,
    fileName             = Some("test.pdf"),
    fileType             = Some("pdf"),
    fileDescription      = Some("pdf file attached"),
    fileSize             = Some(42),
    fileContent          = Some("dGVzdCBmaWxlIGNvbnRlbnQ="),
    updateSequenceNumber = 10
  )

  private val fullJson: JsObject = Json.obj(
    "attachmentNumber"     -> 4,
    "fileName"             -> "test.pdf",
    "fileType"             -> "pdf",
    "fileDescription"      -> "pdf file attached",
    "fileSize"             -> 42,
    "fileContent"          -> "dGVzdCBmaWxlIGNvbnRlbnQ=",
    "updateSequenceNumber" -> 10
  )

  private val mandatoryOnlyJson: JsObject = Json.obj(
    "attachmentNumber"     -> 4,
    "updateSequenceNumber" -> 10
  )

  "GetFullAttachmentDetailsResponse JSON format" should {

    "serialize to JSON correctly" in {
      Json.toJson(fullResponse) shouldBe fullJson
    }

    "deserialize from JSON correctly" in {
      fullJson.as[GetFullAttachmentDetailsResponse] shouldBe fullResponse
    }

    "deserialize when only mandatory fields are present" in {
      mandatoryOnlyJson.as[GetFullAttachmentDetailsResponse] shouldBe fullResponse.copy(
        fileName        = None,
        fileType        = None,
        fileDescription = None,
        fileSize        = None,
        fileContent     = None
      )
    }

    Seq("attachmentNumber", "updateSequenceNumber").foreach { field =>
      s"fail to deserialize when $field is missing" in {
        (mandatoryOnlyJson - field).validate[GetFullAttachmentDetailsResponse].isError shouldBe true
      }
    }
  }
}
