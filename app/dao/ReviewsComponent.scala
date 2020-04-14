package dao

import models.Review

object SQLiteReviewsComponent
  extends ReviewsComponent
    with SQLitePersistence

trait ReviewsComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object ReviewsRepository extends Repository[ReviewsTable, Int](profile, db) {
    val table = Reviews
    def getId(table: ReviewsTable) = table.reviewId
    def setId(review: Review, id: Id) = review.copy(reviewId = id)
  }
}
