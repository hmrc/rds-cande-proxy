package uk.gov.hmrc.rdscandeproxy.euvat.models.responses

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

class SadReferenceCountResponseSpec extends AnyWordSpec with Matchers {

  "SadReferenceCountResponse JSON format" should {

    "serialize to JSON correctly" in {
      val response = SadReferenceCountResponse(duplicateCount = 3)

      val json = Json.toJson(response)

      json shouldBe Json.obj(
        "duplicateCount" -> 3
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "duplicateCount" -> 3
      )

      val result = json.as[SadReferenceCountResponse]

      result shouldBe SadReferenceCountResponse(duplicateCount = 3)
    }
  }
}
