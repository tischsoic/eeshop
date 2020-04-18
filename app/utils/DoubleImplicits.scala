package utils

import play.api.data.FormError
import play.api.data.format.Formatter

object DoubleImplicits {
  implicit def matchFilterFormat: Formatter[Double] = new Formatter[Double] {
    override def bind(key: String, data: Map[String, String]) =
      data.get(key)
        .map(_.toDouble)
        .toRight(Seq(FormError(key, "error.required", Nil)))

    override def unbind(key: String, value: Double) =
      Map(key -> value.toString)
  }

  implicit class Price(d: Double) {
    def truncateTo2Decimals: Double = (math floor d * 100) / 100
  }
}
