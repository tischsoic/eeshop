package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.{User, UserRole}

import scala.concurrent.{ExecutionContext, Future}


class UserServiceImpl @Inject()()(implicit ec: ExecutionContext) extends UserService {
  import dao.SQLiteUserComponent._
  import dao.SQLiteLoginInfosComponent._

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    UserRepository.find(loginInfo)
//    Future.successful(Some(User(0, "em", "a", "b", "pass", UserRole.Customer)))

  /**
   * Retrieves a user and login info pair by userID and login info providerID
   *
   * @param id         The ID to retrieve a user.
   * @param providerID The ID of login info provider.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieveUserLoginInfo(id: Int, providerID: String): Future[Option[(User, LoginInfo)]] = {
    LoginInfosRepository.find(id, providerID)
  }

  /**
   * Creates or updates user
   *
   * If a user exists for given login info or email then update the user, otherwise create a new user with the given data
   *
   * @param loginInfo social profile
   * @param email     user email
   * @param firstName first name
   * @param lastName  last name
   * @param avatarURL avatar URL
   * @return
   */
  def createOrUpdate(loginInfo: LoginInfo,
                              email: String,
                              firstName: Option[String],
                              lastName: Option[String],
                              avatarURL: Option[String]): Future[User] = {

    Future.sequence(Seq(UserRepository.find(loginInfo), UserRepository.findByEmail(email))).flatMap { users =>
      users.flatten.headOption match {
        case Some(user) =>
          UserRepository.save(user.copy(
            firstName = firstName.getOrElse(""),
            lastName = lastName.getOrElse(""),
            email = email,
          ))
        case None =>
          UserRepository.save(User(
            userId = 0,
            firstName = firstName.getOrElse(""),
            lastName = lastName.getOrElse(""),
            email = email,
            role = UserRole.Customer,
            password = ""
          ))
      }
    }
  }
}