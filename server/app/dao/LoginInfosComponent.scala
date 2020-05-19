package dao

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import models.{DBLoginInfo, DBOAuth2Info, DBUserLoginInfo, FaqNote, User}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag


object SQLiteLoginInfosComponent
  extends LoginInfosComponent
    with SQLitePersistence

trait LoginInfosComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object LoginInfosRepository {
    val table = LoginInfos

    /**
     * Saves a login info for user
     *
     * @param userID The user id.
     * @param loginInfo login info
     * @return unit
     */
    def saveUserLoginInfo(userID: Int, loginInfo: LoginInfo)(implicit ec: ExecutionContext): Future[Unit] = {

      val dbLoginInfo = DBLoginInfo(None, loginInfo.providerID, loginInfo.providerKey)
      // We don't have the LoginInfo id so we try to get it first.
      // If there is no LoginInfo yet for this user we retrieve the id on insertion.
      val loginInfoAction = {
        val retrieveLoginInfo = LoginInfos.filter(
          info => info.providerID === loginInfo.providerID &&
            info.providerKey === loginInfo.providerKey).result.headOption
        val insertLoginInfo = LoginInfos.returning(LoginInfos.map(_.id)).
          into((info, id) => info.copy(id = Some(id))) += dbLoginInfo
        for {
          loginInfoOption <- retrieveLoginInfo
          dbLoginInfo <- loginInfoOption.map(DBIO.successful(_)).getOrElse(insertLoginInfo)
        } yield dbLoginInfo
      }

      // combine database actions to be run sequentially
      val actions = (for {
        dbLoginInfo <- loginInfoAction
        userLoginInfo = DBUserLoginInfo(userID, dbLoginInfo.id.get)
        exists <- existsUserLoginInfo(userLoginInfo)
        _ <- if (exists) DBIO.successful(()) else UserLoginInfos += userLoginInfo
      } yield ()).transactionally

      // run actions and return user afterwards
      db.run(actions)
    }

    private def existsUserLoginInfo(uli: DBUserLoginInfo) = {
      UserLoginInfos.filter(e => e.loginInfoId === uli.loginInfoId && e.userID === uli.userID).exists.result
    }


    /**
     * Finds a user, login info pair by userID and login info providerID
     *
     * @param userId     user id
     * @param providerId provider id
     * @return Some(User, LoginInfo) if there is a user by userId which has login method for provider by provider ID, otherwise None
     */
    def find(userId: Int, providerId: String)(implicit ec: ExecutionContext): Future[Option[(User, LoginInfo)]] = {
      val action = for {
        ((_, li), u) <- UserLoginInfos.filter(_.userID === userId)
          .join(LoginInfos).on(_.loginInfoId === _.id)
          .join(Users).on(_._1.userID === _.userId)

        if li.providerID === providerId
      } yield (u, li)

      db.run(action.result.headOption).map(_.map{case (u, li) => (u, DBLoginInfo.toLoginInfo(li))})
    }

    /**
     * Get list of user authentication methods providers
     *
     * @param email user email
     * @return
     */
    def getAuthenticationProviders(email: String): Future[Seq[String]] = {
      val action = for {
        ((_, _), li) <- Users.filter(_.email === email)
          .join(UserLoginInfos).on(_.userId === _.userID)
          .join(LoginInfos).on(_._2.loginInfoId === _.id)
      } yield li.providerID

      db.run(action.result)
    }
  }
}

