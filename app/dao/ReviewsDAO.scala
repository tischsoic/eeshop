package dao

import javax.inject.Inject
import models.Review
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class ReviewsDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends Dao {
  import profile.api._

  def all(): Future[Seq[Review]] = db.run(Reviews.result)

  def insert(review: Review): Future[Review] = {
    val insertQuery = Reviews returning Reviews.map(_.reviewId) into ((review, id) => review.copy(reviewId = id))
    db.run(insertQuery += review)
  }

}

