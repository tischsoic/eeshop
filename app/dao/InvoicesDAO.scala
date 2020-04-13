package dao

import javax.inject.Inject
import models.Invoice
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class InvoicesDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends Dao {
  import profile.api._

  def all(): Future[Seq[Invoice]] = db.run(Invoices.result)

  def insert(invoice: Invoice): Future[Invoice] = {
    val insertQuery = Invoices returning Invoices.map(_.invoiceId) into ((invoice, id) => invoice.copy(invoiceId = id))
    db.run(insertQuery += invoice)
  }

}

