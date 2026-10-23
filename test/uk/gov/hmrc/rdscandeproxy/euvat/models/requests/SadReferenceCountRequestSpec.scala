package uk.gov.hmrc.rdscandeproxy.euvat.models.requests

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

class SadReferenceCountRequestSpec extends AnyWordSpec with Matchers {

  "SadReferenceCountRequest JSON format" should {

    "serialize to JSON correctly" in {
      val request = SadReferenceCountRequest(
        applicationId      = 133,
        itemNumber         = 4,
        sadReferenceNumber = "500000881"
      )

      val json = Json.toJson(request)

      json shouldBe Json.obj(
        "applicationId"      -> 133,
        "itemNumber"         -> 4,
        "sadReferenceNumber" -> "500000881"
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "applicationId"      -> 133,
        "itemNumber"         -> 4,
        "sadReferenceNumber" -> "500000881"
      )

      val result = json.as[SadReferenceCountRequest]

      result shouldBe SadReferenceCountRequest(
        applicationId      = 133,
        itemNumber         = 4,
        sadReferenceNumber = "500000881"
      )
    }
  }
}
