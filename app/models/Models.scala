package models

import java.sql.Date

import models.OrderStatus.OrderStatus
import models.UserRole.UserRole
import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json._
import utils.EnumUtils

case class User(userId: Int, email: String, firstName: String, lastName: String, password: String, role: UserRole)
case class ProductType(productTypeId: Int, name: String, description: String)
case class Product(productId: Int, productTypeId: Int, name: String, price: Double, description: String, quantity: Int)
case class Order(orderId: Int, customerId: Int, status: OrderStatus)
case class OrderItem(orderItemId: Int, orderId: Int, productId: Int, quantity: Int, price: Double)
case class Invoice(invoiceId: Int, orderId: Int, totalCost: Double, date: Date)
case class Payment(paymentId: Int, invoiceId: Int, date: Date, sum: Double)
case class Shipment(shipmentId: Int, orderId: Int, date: Date, trackingCode: String)
case class Review(reviewId: Int, productId: Int, authorId: Int, content: String)
case class FaqNote(faqNoteId: Int, title: String, message: String)

object UserRole extends Enumeration {
  type UserRole = Value
  val Staff: UserRole.Value = Value("staff")
  val Customer: UserRole.Value = Value("customer")

  implicit val enumReads: Reads[UserRole] = EnumUtils.enumReads(UserRole)
  implicit def enumWrites: Writes[UserRole] = EnumUtils.enumWrites

  implicit def matchFilterFormat: Formatter[UserRole] = new Formatter[UserRole] {
    override def bind(key: String, data: Map[String, String]) =
      data.get(key)
        .map(UserRole.withName)
        .toRight(Seq(FormError(key, "error.required", Nil)))

    override def unbind(key: String, value: UserRole) =
      Map(key -> value.toString)
  }
}

object OrderStatus extends Enumeration {
  type OrderStatus = Value
  val AwaitingPayment: OrderStatus.Value = Value("awaiting_payment")
  val Pack: OrderStatus.Value = Value("pack")
  val Sent: OrderStatus.Value = Value("sent")
  val Delivered: OrderStatus.Value = Value("delivered")

  implicit val enumReads: Reads[OrderStatus] = EnumUtils.enumReads(OrderStatus)
  implicit def enumWrites: Writes[OrderStatus] = EnumUtils.enumWrites
}

object User {
  implicit val userJsonFormat = Json.format[User]
  def tupled = (this.apply _).tupled
}

object ProductType {
  implicit val productTypeJsonFormat = Json.format[ProductType]
  def tupled = (this.apply _).tupled
}

object Product {
  implicit val productJsonFormat = Json.format[Product]
  def tupled = (this.apply _).tupled
}

object Order {
  implicit val orderJsonFormat = Json.format[Order]
  def tupled = (this.apply _).tupled
}

object OrderItem {
  implicit val orderItemJsonFormat: OFormat[OrderItem] = Json.format[OrderItem]
  def tupled = (this.apply _).tupled
}

object Invoice {
  implicit val invoiceJsonFormat = Json.format[Invoice]
  def tupled = (this.apply _).tupled
}

object Payment {
  implicit val paymentJsonFormat: OFormat[Payment] = Json.format[Payment]
  def tupled = (this.apply _).tupled
}

object Shipment {
  implicit val shipmentJsonFormat = Json.format[Shipment]
  def tupled = (this.apply _).tupled
}

object Review {
  implicit val reviewJsonFormat = Json.format[Review]
  def tupled = (this.apply _).tupled
}

object FaqNote {
  implicit val faqNoteJsonFormat = Json.format[FaqNote]
  def tupled = (this.apply _).tupled
}
