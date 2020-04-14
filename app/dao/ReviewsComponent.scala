package dao

import models.{Review, User}

object SQLiteReviewsComponent
  extends ReviewsComponent
    with SQLitePersistence

trait ReviewsComponent extends Tables { this: DatabaseComponent with ProfileComponent =>

  import slick.lifted.Tag
  import profile.api._

  //  case class User(userId: Int, email: String, firstName: String, lastName: String, password: String, role: UserRole)
  //
  //  class UsersTable(tag: Tag) extends Table[User](tag, "users") {
  //    implicit val userRoleMapper = MappedColumnType.base[UserRole, String](
  //      e => e.toString,
  //      s => UserRole.withName(s)
  //    )
  //
  //    def userId = column[Int]("user_id", O.PrimaryKey, O.AutoInc)
  //    def email = column[String]("email", O.Unique)
  //    def firstName = column[String]("first_name")
  //    def lastName = column[String]("last_name")
  //    def password = column[String]("password")
  //    def role = column[UserRole]("role")
  //
  //    def * = (userId, email, firstName, lastName, password, role) <> (User.tupled, User.unapply)
  //  }
  //  val Users = Users

  object ReviewsRepository extends Repository[ReviewsTable, Int](profile, db) {
    import this.profile.api._

    val table = Reviews
    def getId(table: ReviewsTable) = table.reviewId
    def setId(review: Review, id: Id) = review.copy(reviewId = id)
  }

}




//object SQLiteReviewsComponent
//  extends ReviewsComponent
//    with SQLitePersistence
//
//trait ReviewsComponent { this: DatabaseComponent with ProfileComponent =>
//
//  import slick.lifted.Tag
//  import profile.api._
//
//  case class Review(reviewId: Int, productId: Int, authorId: Int, content: String)
//
//  class ReviewsTable(tag: Tag) extends Table[Review](tag, "reviews") {
//    def reviewId = column[Int]("review_id", O.PrimaryKey, O.AutoInc)
//    def productId = column[Int]("product_id")
//    def authorId = column[Int]("author_id")
//    def content = column[String]("content")
//
//    def * = (reviewId, productId, authorId, content) <> (Review.tupled, Review.unapply)
//
////    def product =
////      foreignKey("product_id", productId, Products)(_.productId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.Restrict)
//
//        def author =
//          foreignKey("author_id", authorId, Users)(_.userId, onUpdate=ForeignKeyAction.Cascade, onDelete=ForeignKeyAction.SetNull)
//  }
//
//  object ReviewsRepository extends Repository[ReviewsTable, Int](profile, db) {
//    import this.profile.api._
//
//    val table = TableQuery[ReviewsTable]
//    def getId(table: ReviewsTable) = table.reviewId
//    def setId(user: Review, id: Id) = user.copy(reviewId = id)
//  }
//
//}

