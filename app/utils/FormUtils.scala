package utils

object FormUtils {
  import utils.DoubleImplicits._

  val priceConstraint: (Double => Boolean) = v => v > 0 && v == v.truncateTo2Decimals
}
