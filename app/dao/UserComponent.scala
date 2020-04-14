package dao

import models.User

//import dao.UserRole.UserRole

object SQLiteUserComponent
  extends UserComponent
    with SQLitePersistence

//object UserRole extends Enumeration {
//  type UserRole = Value
//  val Staff: UserRole.Value = Value("staff")
//  val Customer: UserRole.Value = Value("customer")
//}

trait UserComponent extends Tables { this: DatabaseComponent with ProfileComponent =>

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

  object UserRepository extends Repository[UsersTable, Int](profile, db) {
    import this.profile.api._

    val table = Users
    def getId(table: UsersTable) = table.userId
    def setId(user: User, id: Id) = user.copy(userId = id)
  }

}

