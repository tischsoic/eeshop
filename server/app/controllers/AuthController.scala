package controllers

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.{Clock, PlayHTTPLayer}
import com.mohiva.play.silhouette.crypto.{JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import com.mohiva.play.silhouette.impl.providers.state.{CsrfStateItemHandler, CsrfStateSettings}
import com.mohiva.play.silhouette.impl.providers.{DefaultSocialStateHandler, OAuth2Settings, SocialProviderRegistry}
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import com.typesafe.config.Config
import models.services.{AccountBound, AuthenticateService, EmailIsBeingUsed, NoEmailProvided, UserService}
import utils.DefaultEnv

import scala.concurrent.duration.FiniteDuration
import com.mohiva.play.silhouette.impl.providers.oauth2.GoogleProvider
import javax.inject._
import models.DeleteForm.deleteForm
import models.OrderStatus.OrderStatus
import models._
import play.api.Configuration
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader._
import net.ceedubs.ficus.readers.ValueReader


import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthController @Inject()(
                                cc: MessagesControllerComponents,
                                ws: WSClient,
                                configuration: Configuration,
                                silhouette: Silhouette[DefaultEnv],
                                clock: Clock,
                                userService: UserService,
                                authenticateService: AuthenticateService,
                                authInfoRepository: AuthInfoRepository,
                                gp: GoogleProvider,
                                fp: FacebookProvider)
                              (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  import dao.SQLiteOrderItemsComponent._
  import dao.SQLiteOrdersComponent._
  import dao.SQLiteProductsComponent._
  import dao.SQLiteUserComponent._

  implicit val sameSiteReader: ValueReader[Option[Option[Cookie.SameSite]]] =
    (config: Config, path: String) => {
      if (config.hasPathOrNull(path)) {
        if (config.getIsNull(path))
          Some(None)
        else {
          Some(Cookie.SameSite.parse(config.getString(path)))
        }
      } else {
        None
      }
    }

  def auth(provider: String) = Action.async { implicit request: Request[AnyContent] =>
    val p = provider match {
      case "google" => gp
      case "facebook" => fp
    }

    p.authenticate().flatMap {
      case Left(result) => Future.successful(result)
      case Right(authInfo) => for {
        profile <- p.retrieveProfile(authInfo)
        userBindResult <- authenticateService.provideUserForSocialAccount(provider, profile, authInfo)
        result <- userBindResult match {
          case AccountBound(u) =>
            authenticateUser(u, profile.loginInfo, rememberMe = true)
          case EmailIsBeingUsed(providers) =>
            Future.successful(Conflict(Json.obj("error" -> "EmailIsBeingUsed", "providers" -> providers)))
          case NoEmailProvided =>
            Future.successful(BadRequest(Json.obj("error" -> "NoEmailProvided")))
        }
      } yield result
    }
  }

  protected def authenticateUser(user: User, loginInfo: LoginInfo, rememberMe: Boolean)(implicit request: Request[_]): Future[AuthenticatorResult] = {
    silhouette.env.authenticatorService.create(loginInfo).flatMap { authenticator =>
      silhouette.env.eventBus.publish(LoginEvent(user, request))
      silhouette.env.authenticatorService.init(authenticator).flatMap { token =>
        silhouette.env.authenticatorService.embed(token, Ok(Json.obj(
          "id" -> user.userId,
          "token" -> token,
          "firstName" -> user.firstName,
          "lastName" -> user.lastName,
          "role" -> user.role,
          "email" -> user.email
        )))
      }
    }
  }

  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, Ok)
  }

}
