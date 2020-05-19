package models.services

import java.time.Instant

import akka.actor.ActorRef
import akka.util.Timeout
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.{AuthInfo, LoginInfo}
import com.mohiva.play.silhouette.impl.providers._
import javax.inject.{Inject, Named}
import models.User

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 *
 * @param userService             The user service implementation.
 * @param credentialsProvider     The credentials provider.
// * @param bruteForceDefenderActor actor that tracks failed user signins and decides whether user allowed to sign in
 * @param authInfoRepository      The auth info repository implementation.
 * @param ec                      The execution context.
 */
class AuthenticateService @Inject()(credentialsProvider: CredentialsProvider,
                                    userService: UserService,
                                    authInfoRepository: AuthInfoRepository,
//                                    socialProviderRegistry: SocialProviderRegistry
                                   )(implicit ec: ExecutionContext) {
  import dao.SQLiteLoginInfosComponent._
  implicit val timeout: Timeout = 5.seconds

  /**
   * Creates or fetches existing user for given social profile and binds it with given auth info
   *
   * @param provider social authentication provider
   * @param profile  social profile data
   * @param authInfo authentication info
   * @tparam T type of authentication info
   * @return
   *         NoEmailProvided if social profile email is empty
   *         EmailIsBeingUsed if there is existing user with email which eq to given social profile email and user
   *           has no authentication providers for given provider
   *         AccountBind if everything is ok
   */
  def provideUserForSocialAccount[T <: AuthInfo](provider: String, profile: CommonSocialProfile, authInfo: T): Future[UserForSocialAccountResult] = {
    profile.email match {
      case Some(email) =>
        LoginInfosRepository.getAuthenticationProviders(email).flatMap { providers =>
          if (providers.contains(provider) || providers.isEmpty) {
            for {
              user <- userService.createOrUpdate(
                profile.loginInfo,
                email,
                profile.firstName,
                profile.lastName,
                profile.avatarURL
              )
              _ <- addAuthenticateMethod(user.userId, profile.loginInfo, authInfo)
            } yield AccountBound(user)
          } else {
            Future.successful(EmailIsBeingUsed(providers))
          }
        }
      case None =>
        Future.successful(NoEmailProvided)
    }
  }

  /**
   * Adds authentication method to user
   *
   * @param userId    user id
   * @param loginInfo login info
   * @param authInfo  auth info
   * @tparam T tyupe of auth info
   * @return
   */
  def addAuthenticateMethod[T <: AuthInfo](userId: Int, loginInfo: LoginInfo, authInfo: T): Future[Unit] = {
    for {
      _ <- LoginInfosRepository.saveUserLoginInfo(userId, loginInfo)
      _ <- authInfoRepository.add(loginInfo, authInfo)
    } yield ()
  }

  /**
   * Checks whether user have authentication method for given provider id
   *
   * @param userId     user id
   * @param providerId authentication provider id
   * @return true if user has authentication method for given provider id, otherwise false
   */
  def userHasAuthenticationMethod(userId: Int, providerId: String): Future[Boolean] = {
    LoginInfosRepository.find(userId, providerId).map(_.nonEmpty)
  }

  /**
   * Get list of providers of user authentication methods
   *
   * @param email user email
   * @return
   */
  def getAuthenticationProviders(email: String): Future[Seq[String]] =
    LoginInfosRepository.getAuthenticationProviders(email)
}

sealed trait AuthenticateResult
case class Success(user: User) extends AuthenticateResult
case class InvalidPassword(attemptsAllowed: Int) extends AuthenticateResult
object NonActivatedUserEmail extends AuthenticateResult
object UserNotFound extends AuthenticateResult
case class ToManyAuthenticateRequests(nextAllowedAttemptTime: Instant) extends AuthenticateResult

sealed trait UserForSocialAccountResult
case object NoEmailProvided extends UserForSocialAccountResult
case class EmailIsBeingUsed(providers: Seq[String]) extends UserForSocialAccountResult
case class AccountBound(user: User) extends UserForSocialAccountResult
