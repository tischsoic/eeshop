package dao

import javax.inject.Inject
import models.Payment
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class PaymentsDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends Dao {
  import profile.api._

  def all(): Future[Seq[Payment]] = db.run(Payments.result)

  def insert(payment: Payment): Future[Payment] = {
    val insertQuery = Payments returning Payments.map(_.paymentId) into ((payment, id) => payment.copy(paymentId = id))
    db.run(insertQuery += payment)
  }

}

