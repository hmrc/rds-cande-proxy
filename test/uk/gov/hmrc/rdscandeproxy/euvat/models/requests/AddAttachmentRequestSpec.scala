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
    fileSize             = 5L,
    fileContent          = fileContent,
    updateSequenceNumber = 1
  )

  "AddAttachmentRequest JSON format" should {

    "serialize and deserialize correctly" in {
      val result = Json.toJson(request).as[AddAttachmentRequest]

      result.applicationId shouldBe request.applicationId
      result.fileName shouldBe request.fileName
      result.fileType shouldBe request.fileType
      result.fileDescription shouldBe request.fileDescription
      result.fileSize shouldBe request.fileSize
      result.fileContent should contain theSameElementsInOrderAs request.fileContent
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

      result.applicationId shouldBe 404L
      result.fileName shouldBe "invoice.pdf"
      result.fileType shouldBe "application/pdf"
      result.fileDescription shouldBe Some("Supporting invoice")
      result.fileSize shouldBe 5L
      result.fileContent should contain theSameElementsInOrderAs Array[Byte](1, 2, 3, 4, 5)
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