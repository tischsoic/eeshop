package dao

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import models.DBOAuth2Info
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/**
 * The DAO to store the OAuth2 information.
 */
class OAuth2InfoDAO @Inject() ()(implicit ec: ExecutionContext, val classTag: ClassTag[OAuth2Info])
  extends DelegableAuthInfoDAO[OAuth2Info] {
  import dao.SQLiteOAuth2InfosComponent._

  import profile.api._

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] =
    OAuth2InfosRepository.find(loginInfo)

  /**
   * Adds new auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be added.
   * @param authInfo  The auth info to add.
   * @return The added auth info.
   */
  def add(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] =
    OAuth2InfosRepository.add(loginInfo, authInfo)

  /**
   * Updates the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be updated.
   * @param authInfo  The auth info to update.
   * @return The updated auth info.
   */
  def update(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] =
    OAuth2InfosRepository.update(loginInfo, authInfo)

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
  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] =
    OAuth2InfosRepository.save(loginInfo, authInfo)

  /**
   * Removes the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(loginInfo: LoginInfo): Future[Unit] =
    OAuth2InfosRepository.remove(loginInfo)
}