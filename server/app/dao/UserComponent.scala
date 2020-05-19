package dao

import com.mohiva.play.silhouette.api.LoginInfo
import models.{User, UserRole}

import scala.concurrent.{ExecutionContext, Future}

object SQLiteUserComponent
  extends UserComponent
    with SQLitePersistence

trait UserComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object UserRepository extends Repository[UsersTable, Int](profile, db) {
    val table = Users
    def getId(table: UsersTable) = table.userId
    def setId(user: User, id: Id) = user.copy(userId = id)

    //////////////////////////


    /**
     * Finds a user by its login info.
     *
     * @param loginInfo The login info of the user to find.
     * @return The found user or None if no user for the given login info could be found.
     */
    def find(loginInfo: LoginInfo)(implicit ec: ExecutionContext) = {
      val userQuery = for {
        dbLoginInfo <- loginInfoQuery(loginInfo)
        dbUserLoginInfo <- UserLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
        dbUser <- Users.filter(_.userId === dbUserLoginInfo.userID)
      } yield dbUser
      db.run(userQuery.result.headOption)
    }

    /**
     * Finds a user by its user ID.
     *
     * @param userID The ID of the user to find.
     * @return The found user or None if no user for the given ID could be found.
     */
    def find(userID: Int) = {
      val query = Users.filter(_.userId === userID)

      db.run(query.result.headOption)
    }

    /**
     * Saves a user.
     *
     * @param user The user to save.
     * @return The saved user.
     */
    def save(user: User)(implicit ec: ExecutionContext): Future[User] = {
      val a: Future[Option[Int]] = db.run((Users returning Users.map(_.userId)).insertOrUpdate(user))
      a.map({
        case None => user
        case Some(userId) => user.copy(userId = userId)
      })
    }

//    /**
//     * Updates user role
//     *
//     * @param userId user id
//     * @param role   user role to update to
//     * @return
//     */
//    override def updateUserRole(userId: Int, role: UserRoles.UserRole): Future[Boolean] = {
//      db.run(Users.filter(_.id === userId).map(_.roleId).update(role.id)).map(_ > 0)
//    }

    /**
     * Finds a user by its email
     *
     * @param email email of the user to find
     * @return The found user or None if no user for the given login info could be found
     */
    def findByEmail(email: String): Future[Option[User]] = {
      db.run(Users.filter(_.email === email).take(1).result.headOption)
    }
  }
}
