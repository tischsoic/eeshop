package dao

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import models.{DBOAuth2Info, FaqNote}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag


object SQLiteOAuth2InfosComponent
  extends OAuth2InfosComponent
    with SQLitePersistence

trait OAuth2InfosComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object OAuth2InfosRepository {
    val table = OAuth2Infos

    protected def oAuth2InfoQuery(loginInfo: LoginInfo) = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbOAuth2Info <- OAuth2Infos if dbOAuth2Info.loginInfoId === dbLoginInfo.id
    } yield dbOAuth2Info

    // Use subquery workaround instead of join to get authinfo because slick only supports selecting
    // from a single table for update/delete queries (https://github.com/slick/slick/issues/684).
    protected def oAuth2InfoSubQuery(loginInfo: LoginInfo) =
      table.filter(_.loginInfoId in loginInfoQuery(loginInfo).map(_.id))

    protected def addAction(loginInfo: LoginInfo, authInfo: OAuth2Info)(implicit executor: ExecutionContext) =
      loginInfoQuery(loginInfo).result.head.flatMap { dbLoginInfo =>
        table += DBOAuth2Info(
          None,
          authInfo.accessToken,
          authInfo.tokenType,
          authInfo.expiresIn,
          authInfo.refreshToken,
          dbLoginInfo.id.get)
      }.transactionally

    def updateAction(loginInfo: LoginInfo, authInfo: OAuth2Info) =
      oAuth2InfoSubQuery(loginInfo).
        map(dbOAuth2Info => (dbOAuth2Info.accessToken, dbOAuth2Info.tokenType, dbOAuth2Info.expiresIn, dbOAuth2Info.refreshToken)).
        update((authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken))

    /**
     * Finds the auth info which is linked with the specified login info.
     *
     * @param loginInfo The linked login info.
     * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
     */
    def find(loginInfo: LoginInfo)(implicit ec: ExecutionContext): Future[Option[OAuth2Info]] = {
      val result = db.run(oAuth2InfoQuery(loginInfo).result.headOption)
      result.map { dbOAuth2InfoOption =>
        dbOAuth2InfoOption.map { dbOAuth2Info: DBOAuth2Info =>
          OAuth2Info(dbOAuth2Info.accessToken, dbOAuth2Info.tokenType, dbOAuth2Info.expiresIn, dbOAuth2Info.refreshToken)
        }
      }
    }

    /**
     * Adds new auth info for the given login info.
     *
     * @param loginInfo The login info for which the auth info should be added.
     * @param authInfo  The auth info to add.
     * @return The added auth info.
     */
    def add(loginInfo: LoginInfo, authInfo: OAuth2Info)(implicit ec: ExecutionContext): Future[OAuth2Info] =
      db.run(addAction(loginInfo, authInfo)).map(_ => authInfo)

    /**
     * Updates the auth info for the given login info.
     *
     * @param loginInfo The login info for which the auth info should be updated.
     * @param authInfo  The auth info to update.
     * @return The updated auth info.
     */
    def update(loginInfo: LoginInfo, authInfo: OAuth2Info)(implicit ec: ExecutionContext): Future[OAuth2Info] =
      db.run(updateAction(loginInfo, authInfo)).map(_ => authInfo)

    /**
     * Saves the auth info for the given login info.
     *
     * This method either adds the auth info if it doesn't exists or it updates the auth info
     * if it already exists.
     *
     * @param loginInfo The login info for which the auth info should be saved.
     * @param authInfo  The auth info to save.
     * @return The saved auth info.
     */
    def save(loginInfo: LoginInfo, authInfo: OAuth2Info)(implicit ec: ExecutionContext): Future[OAuth2Info] = {
      val query = for {
        result <- loginInfoQuery(loginInfo).joinLeft(table).on(_.id === _.loginInfoId)
      } yield result
      val action = query.result.head.flatMap {
        case (dbLoginInfo, Some(dbOAuth2Info)) => updateAction(loginInfo, authInfo)
        case (dbLoginInfo, None) => addAction(loginInfo, authInfo)
      }.transactionally
      db.run(action).map(_ => authInfo)
    }

    /**
     * Removes the auth info for the given login info.
     *
     * @param loginInfo The login info for which the auth info should be removed.
     * @return A future to wait for the process to be completed.
     */
    def remove(loginInfo: LoginInfo)(implicit ec: ExecutionContext): Future[Unit] = {
//      val q: Query[LoginInfosTable, LoginInfosTable#TableElementType, Seq] = LoginInfos.filter(li => li.providerID === loginInfo.providerID && li.providerKey === loginInfo.providerKey)
//      db.run(q.delete).map(_ => ())
      db.run(oAuth2InfoSubQuery(loginInfo).delete).map(_ => ())
    }
  }
}

