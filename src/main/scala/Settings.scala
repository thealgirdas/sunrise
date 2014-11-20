import org.joda.time.{DateTimeZone, Duration, LocalTime}

import scala.concurrent.duration._


// A simple container for settings. Possibly refactoring toward this: https://github.com/typesafehub/config#schemas-and-validation

object Settings {
  val localTimeZone = DateTimeZone.forID("America/Los_Angeles")

  val dawnStart = new LocalTime(6, 0)
  val dawnLength = new Duration(3 * 60 * 60 * 1000)
  val sunsetStart = new LocalTime(18, 30)
  val sunsetLength = new Duration(3 * 60 * 60 * 1000)

  val updateFrequency = 15.seconds
}