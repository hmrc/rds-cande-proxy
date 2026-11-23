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

package uk.gov.hmrc.rdscandeproxy.euvat.models.requests

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class AddAttachmentRequestSpec extends AnyWordSpec with Matchers {

  private val fileContent: Array[Byte] =
    Array[Byte](1, 2, 3, 4, 5)

  private val request = AddAttachmentRequest(
    applicationId        = 404L,
    fileName             = "invoice.pdf",
    fileType             = "application/pdf",
    fileDescription      = Some("Supporting invoice"),
    fileSize             = 5,
    fileContent          = fileContent,
    updateSequenceNumber = 1
  )

  "AddAttachmentRequest JSON format" should {

    "serialize and deserialize correctly" in {
      val result = Json.toJson(request).as[AddAttachmentRequest]

      result.applicationId        shouldBe request.applicationId
      result.fileName             shouldBe request.fileName
      result.fileType             shouldBe request.fileType
      result.fileDescription      shouldBe request.fileDescription
      result.fileSize             shouldBe request.fileSize
      result.fileContent            should contain theSameElementsInOrderAs request.fileContent
      result.updateSequenceNumber shouldBe request.updateSequenceNumber
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "applicationId"        -> 404L,
        "fileName"             -> "invoice.pdf",
        "fileType"             -> "application/pdf",
        "fileDescription"      -> "Supporting invoice",
        "fileSize"             -> 5L,
        "fileContent"          -> Json.arr(1, 2, 3, 4, 5),
        "updateSequenceNumber" -> 1
      )

      val result = json.as[AddAttachmentRequest]

      result.applicationId        shouldBe 404L
      result.fileName             shouldBe "invoice.pdf"
      result.fileType             shouldBe "application/pdf"
      result.fileDescription      shouldBe Some("Supporting invoice")
      result.fileSize             shouldBe 5L
      result.fileContent            should contain theSameElementsInOrderAs Array[Byte](1, 2, 3, 4, 5)
      result.updateSequenceNumber shouldBe 1
    }

    "deserialize correctly when fileDescription is absent" in {
      val json = Json.obj(
        "applicationId"        -> 404L,
        "fileName"             -> "invoice.pdf",
        "fileType"             -> "application/pdf",
        "fileSize"             -> 5L,
        "fileContent"          -> Json.arr(1, 2, 3, 4, 5),
        "updateSequenceNumber" -> 1
      )

      val result = json.as[AddAttachmentRequest]

      result.fileDescription shouldBe None
    }
  }
}
