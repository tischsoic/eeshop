package models

import java.sql.Date

import models.OrderStatus.OrderStatus
import models.UserRole.UserRole
import play.api.libs.json.{Reads, Writes}

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
}

object OrderStatus extends Enumeration {
  type OrderStatus = Value
  val AwaitingPayment: OrderStatus.Value = Value("awaiting_payment")
  val Pack: OrderStatus.Value = Value("pack")
  val Sent: OrderStatus.Value = Value("sent")
  val Delivered: OrderStatus.Value = Value("delivered")

  implicit val readsOrderStatus = Reads.enumNameReads(OrderStatus)
  implicit val writesOrderStatus = Writes.enumNameWrites
}
