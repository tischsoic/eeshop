import com.google.inject.AbstractModule
import java.time.Clock

import com.google.inject.{AbstractModule, Provides}
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder}
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorService, JWTAuthenticatorSettings}
import com.typesafe.config.Config
import models.services.{UserService, UserServiceImpl}
import net.ceedubs.ficus.readers.ValueReader
import services.{ApplicationTimer, AtomicCounter, Counter}
import utils.DefaultEnv
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.mvc.Cookie

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module extends AbstractModule {

  override def configure() = {
    // Use the system clock as the default implementation of Clock
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    // Ask Guice to create an instance of ApplicationTimer when the
    // application starts.
    bind(classOf[ApplicationTimer]).asEagerSingleton()
    // Set AtomicCounter as the implementation for Counter.
    bind(classOf[Counter]).to(classOf[AtomicCounter])



//    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
//    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
//    bind[UserService].to[UserServiceImpl]
  }

//  implicit val sameSiteReader: ValueReader[Option[Option[Cookie.SameSite]]] =
//    (config: Config, path: String) => {
//      if (config.hasPathOrNull(path)) {
//        if (config.getIsNull(path))
//          Some(None)
//        else {
//          Some(Cookie.SameSite.parse(config.getString(path)))
//        }
//      } else {
//        None
//      }
//    }
//
//  @Provides
//  def provideEnvironment(userService: UserService,
//                         authenticatorService: AuthenticatorService[JWTAuthenticator],
//                         eventBus: EventBus): Environment[DefaultEnv] = {
//
//    Environment[DefaultEnv](
//      userService,
//      authenticatorService,
//      Seq(),
//      eventBus
//    )
//  }
//
//  @Provides
//  def provideAuthenticatorService(@Named("authenticator-crypter") crypter: Crypter,
//                                  idGenerator: IDGenerator,
//                                  configuration: Configuration,
//                                  clock: Clock): AuthenticatorService[JWTAuthenticator] = {
//
//    val config = configuration.underlying.as[JWTAuthenticatorSettings]("silhouette.authenticator")
//    val encoder = new CrypterAuthenticatorEncoder(crypter)
//
//    new JWTAuthenticatorService(config, None, encoder, idGenerator, clock)
//  }

}
