package dao

import models.Payment

object SQLitePaymentsComponent
  extends PaymentsComponent
    with SQLitePersistence

trait PaymentsComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object PaymentsRepository extends Repository[PaymentsTable, Int](profile, db) {
    val table = Payments
    def getId(table: PaymentsTable) = table.paymentId
    def setId(product: Payment, id: Id) = product.copy(paymentId = id)
  }
}
