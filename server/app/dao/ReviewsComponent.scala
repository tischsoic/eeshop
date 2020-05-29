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

    def getReview(reviewId: Id) = {
      val query = for {
        (review, user) <- table filter (getId(_) === reviewId) joinLeft Users on (_.authorId === _.userId)
      } yield (review, user)

      db.run(query.result.headOption)
    }

    def getAllReviews() = {
      val query = for {
        (review, user) <- Reviews joinLeft Users on (_.authorId === _.userId)
      } yield (review, user)

      db.run(query.result)
    }

    def getAllReviewsForProduct(productId: Int) = {
      val query = for {
        (review, user) <- Reviews filter (_.productId === productId) joinLeft Users on (_.authorId === _.userId)
      } yield (review, user)

      db.run(query.result)
    }
  }
}
