package dao

import models.Invoice

object SQLiteInvoicesComponent
  extends InvoicesComponent
    with SQLitePersistence

trait InvoicesComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object InvoicesRepository extends Repository[InvoicesTable, Int](profile, db) {
    val table = Invoices
    def getId(table: InvoicesTable) = table.invoiceId
    def setId(product: Invoice, id: Id) = product.copy(invoiceId = id)
  }
}
