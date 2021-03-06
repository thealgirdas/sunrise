package hue

import light.LightOutput
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import play.api.libs.json.{JsObject, Json}

class LightTest extends FlatSpec with ShouldMatchers {

  def fixture = new {
    val api = new FakeApiConnector
    val light = new Bridge(api, "dummykey").light(1)
  }

  "A light" should "know whether it's on" in {
    val f = fixture

    f.api.nextGetResponse = Json.obj("state" -> Json.obj("on" -> false))
    f.light.on should be(false)

    f.api.nextGetResponse = Json.obj("state" -> Json.obj("on" -> true))
    f.light.on should be(true)

    f.api.lastPath should endWith("/lights/1")
  }

  it should "be color-settable" in {
    val f = fixture

    f.light.set(Color(0.65, 0.32))
    f.api.lastPath should endWith("/lights/1/state")
    f.api.lastData should be(Json.obj("xy" -> Json.arr(0.65, 0.32)))
  }

  it should "set multiple parameters" in {
    val f = fixture

    f.light.set(Color(0.65, 0.32), On(true), Brightness(127))
    f.api.lastPath should endWith("/lights/1/state")
    f.api.lastData should be(Json.obj("xy" -> Json.arr(0.65, 0.32), "on" -> true, "bri" -> 127))
  }

  it should "set light output" in {
    val f = fixture
    f.api.addResponse("/lights/1", Json.obj("modelid" -> "LST001", "state" -> Json.obj("on" -> false)))

    f.light.set(LightOutput(0.3, 0.3, 120))
    f.api.lastPath should endWith("/lights/1/state")
    f.api.lastData should be(Json.obj("xy" -> Json.arr(0.3, 0.3), "on" -> true, "bri" -> 255, "transitiontime" -> 10))
  }

  it should "know useful facts" in {
    val f = fixture
    f.api.nextGetResponse = FullLightResponse.as[JsObject]
    f.light.model should be("LCT001")
    f.api.lastPath should endWith("/lights/1")
  }

  it should "know the light output for known bulbs" in {
    val f = fixture
    f.api.addResponse("/lights/1", Json.obj("modelid" -> "LCT001"))
    f.light.maxLumens should be(600)
    f.light.minLumens should be(30) // from specs
    f.api.addResponse("/lights/1", Json.obj("modelid" -> "LST001"))
    f.light.maxLumens should be(120)
    f.light.minLumens should be(6) // estimated
    f.api.addResponse("/lights/1", Json.obj("modelid" -> "LWB004"))
    f.light.maxLumens should be(750)
    f.light.minLumens should be(37) // estimated
  }

  it should "know what output it can promise" in {
    val f = fixture
    f.api.addResponse("/lights/1", Json.obj("modelid" -> "LST001")) // max 120; min 120 * 0.05 = 6

    f.light.closestOutput(LightOutput(0.3, 0.3, 1)) should be(LightOutput(0.3, 0.3, 0))
    f.light.closestOutput(LightOutput(0.3, 0.3, 10)) should be(LightOutput(0.3, 0.3, 10))
    f.light.closestOutput(LightOutput(0.3, 0.3, 100)) should be(LightOutput(0.3, 0.3, 100))
    f.light.closestOutput(LightOutput(0.3, 0.3, 200)) should be(LightOutput(0.3, 0.3, 120))
  }


  val FullLightResponse = Json.parse( """{
                                        	"state": {
                                        		"on": false,
                                        		"bri": 254,
                                        		"hue": 34530,
                                        		"sat": 240,
                                        		"xy": [
                                        			0.3125,
                                        			0.3227
                                        		],
                                        		"ct": 153,
                                        		"alert": "none",
                                        		"effect": "none",
                                        		"colormode": "xy",
                                        		"reachable": true
                                        	},
                                        	"type": "Extended color light",
                                        	"name": "Bed 1",
                                        	"modelid": "LCT001",
                                        	"swversion": "66009663",
                                        	"pointsymbol": {
                                        		"1": "none",
                                        		"2": "none",
                                        		"3": "none",
                                        		"4": "none",
                                        		"5": "none",
                                        		"6": "none",
                                        		"7": "none",
                                        		"8": "none"
                                        	}
                                        }""")

}
