package uk.gov.hmrc.rdscandeproxy.euvat.models.responses

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class AddAttachmentResponseSpec extends AnyWordSpec with Matchers {

  private val response = AddAttachmentResponse(
    attachmentNumber     = 4,
    updateSequenceNumber = 2
  )

  "AddAttachmentResponse JSON format" should {

    "serialize and deserialize correctly" in {
      Json.toJson(response).as[AddAttachmentResponse] shouldBe response
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "attachmentNumber"     -> 4,
        "updateSequenceNumber" -> 2
      )

      json.as[AddAttachmentResponse] shouldBe
        AddAttachmentResponse(
          attachmentNumber = 4,
          updateSequenceNumber = 2
        )
    }
  }
}