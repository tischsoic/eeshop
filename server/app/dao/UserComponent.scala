package dao

import models.User

object SQLiteUserComponent
  extends UserComponent
    with SQLitePersistence

trait UserComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object UserRepository extends Repository[UsersTable, Int](profile, db) {
    val table = Users
    def getId(table: UsersTable) = table.userId
    def setId(user: User, id: Id) = user.copy(userId = id)
  }
}
