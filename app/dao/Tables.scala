package dao

import java.sql.Date
import java.text.SimpleDateFormat

import models.OrderStatus.OrderStatus
import models._
import models.UserRole.UserRole

trait Tables { this: DatabaseComponent with ProfileComponent =>

  import profile.api._

  lazy val Users = TableQuery[UsersTable]
  lazy val ProductTypes = TableQuery[ProductTypesTable]
  lazy val Products = TableQuery[ProductsTable]
  lazy val Orders = TableQuery[OrdersTable]
  lazy val OrderItems = TableQuery[OrderItemsTable]
  lazy val Invoices = TableQuery[InvoicesTable]
  lazy val Payments = TableQuery[PaymentsTable]
  lazy val Shipments = TableQuery[ShipmentsTable]
  lazy val Reviews = TableQuery[ReviewsTable]
  lazy val FaqNotes = TableQuery[FaqNotesTable]

  val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
  // lazy because otherwise MappedColumnType is null:
  lazy implicit val dateColumnType = MappedColumnType.base[Date, String](
    { date => sdf.format(date) },
    { dateStr => new Date(sdf.parse(dateStr).getTime) } // map Int to Bool
  )

  class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    implicit val userRoleMapper = MappedColumnType.base[UserRole, String](
      e => e.toString,
      s => UserRole.withName(s)
    )

    def userId = column[Int]("user_id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email", O.Unique)
    def firstName = column[String]("first_name")
    def lastName = column[String]("last_name")
    def password = column[String]("password")
    def role = column[UserRole]("role")

    def * = (userId, email, firstName, lastName, password, role) <> (User.tupled, User.unapply)
  }

  class ProductTypesTable(tag: Tag) extends Table[ProductType](tag, "product_types") {
    def productTypeId = column[Int]("product_type_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")

    def * = (productTypeId, name, description) <> (ProductType.tupled, ProductType.unapply)
  }

  class ProductsTable(tag: Tag) extends Table[Product](tag, "products") {
    def productId = column[Int]("product_id", O.PrimaryKey, O.AutoInc)
    def productTypeId = column[Int]("product_type_id")
    def name = column[String]("name")
    def price = column[Double]("price")
    def description = column[String]("description")
    def quantity = column[Int]("quantity")

    def * = (productId, productTypeId, name, price, description, quantity) <> (Product.tupled, Product.unapply)

    def productType =
      foreignKey("product_type_id", productTypeId, ProductTypes)(_.productTypeId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
  }

  class OrdersTable(tag: Tag) extends Table[Order](tag, "orders") {
    // TODO: move this mapper to enum object???
    implicit val orderStatusMapper = MappedColumnType.base[OrderStatus, String](
      _.toString,
      OrderStatus.withName
    )

    def orderId = column[Int]("order_id", O.PrimaryKey, O.AutoInc)
    def customerId = column[Int]("customer_id")
    def status = column[OrderStatus]("status")

    def * = (orderId, customerId, status) <> (Order.tupled, Order.unapply)

    def customer =
      foreignKey("customer_id", customerId, Users)(_.userId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
  }

  class OrderItemsTable(tag: Tag) extends Table[OrderItem](tag, "order_items") {
    def orderItemId = column[Int]("order_item_id", O.PrimaryKey, O.AutoInc)
    def orderId = column[Int]("order_id")
    def productId = column[Int]("product_id")
    def quantity = column[Int]("quantity")
    def price = column[Double]("price")

    def * = (orderItemId, orderId, productId, quantity, price) <> (OrderItem.tupled, OrderItem.unapply)

    def order =
      foreignKey("order_id", orderId, Orders)(_.orderId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)

    def product =
      foreignKey("product_id", productId, Products)(_.productId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
  }

  class InvoicesTable(tag: Tag) extends Table[Invoice](tag, "invoices") {
    def invoiceId = column[Int]("invoice_id", O.PrimaryKey, O.AutoInc)
    def orderId = column[Int]("order_id")
    def totalCost = column[Double]("total_cost")
    def date = column[Date]("date")(dateColumnType)

    def * = (invoiceId, orderId, totalCost, date) <> (Invoice.tupled, Invoice.unapply)

    def order =
      foreignKey("order_id", orderId, Orders)(_.orderId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }

  class PaymentsTable(tag: Tag) extends Table[Payment](tag, "payments") {
    def paymentId = column[Int]("payment_id", O.PrimaryKey, O.AutoInc)
    def invoiceId = column[Int]("invoice_id")
    def date = column[Date]("date")(dateColumnType)
    def sum = column[Double]("sum")

    def * = (paymentId, invoiceId, date, sum) <> (Payment.tupled, Payment.unapply)

    def invoice =
      foreignKey("order_id", invoiceId, Invoices)(_.orderId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }

  class ShipmentsTable(tag: Tag) extends Table[Shipment](tag, "shipments") {
    def shipmentId = column[Int]("shipment_id", O.PrimaryKey, O.AutoInc)
    def orderId = column[Int]("order_id")
    def date = column[Date]("date")(dateColumnType)
    def trackingCode = column[String]("tracking_code")

    def * = (shipmentId, orderId, date, trackingCode) <> (Shipment.tupled, Shipment.unapply)

    def order =
      foreignKey("order_id", orderId, Orders)(_.orderId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Cascade)
  }

  class ReviewsTable(tag: Tag) extends Table[Review](tag, "reviews") {
    def reviewId = column[Int]("review_id", O.PrimaryKey, O.AutoInc)
    def productId = column[Int]("product_id")
    def authorId = column[Int]("author_id")
    def content = column[String]("content")

    def * = (reviewId, productId, authorId, content) <> (Review.tupled, Review.unapply)

    def product =
      foreignKey("product_id", productId, Products)(_.productId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)

    def author =
      foreignKey("author_id", authorId, Users)(_.userId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.SetNull)
  }

  class FaqNotesTable(tag: Tag) extends Table[FaqNote](tag, "faq_notes") {
    def faqNoteId = column[Int]("faq_note_id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def message = column[String]("message")

    def * = (faqNoteId, title, message) <> (FaqNote.tupled, FaqNote.unapply)
  }

}
