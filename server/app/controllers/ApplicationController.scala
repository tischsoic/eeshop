package controllers

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import javax.inject.Inject
import models.services.IndexRenderService
import play.api.{Environment, Mode}
import play.api.http.ContentTypes
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc._
import utils.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

/**
 * The basic application controller.
 *
 * @param components The Play controller components.
 * @param silhouette The Silhouette stack.
 */
class ApplicationController @Inject()(components: ControllerComponents,
                                      silhouette: Silhouette[DefaultEnv],
                                      environment: Environment,
                                      ws: WSClient,
                                     )(implicit ec: ExecutionContext)
  extends AbstractController(components) with I18nSupport {

  /**
   * @return vuejs index.html page with CSRF set
   */
  def reactApp(path: String) = silhouette.UserAwareAction.async { implicit req =>
    fetchWebpackServer(path)
  }

  /**
   * Retrieves resource from WebPack server. CSRF token will be injected to HTML files.
   *
   * @param path    HTTP resource path
   * @param request HTTP request
   * @return
   */
  private def fetchWebpackServer(path: String)(implicit request: RequestHeader) = {
    ws.url(s"http://localhost:3000/$path").get().map { r =>
      if (r.contentType.equalsIgnoreCase(HTML(Codec.utf_8))) {
        val html = r.bodyAsBytes.utf8String

        Ok(html).as(ContentTypes.HTML)
      } else {
        new Status(r.status)(r.bodyAsBytes).as(r.contentType)
      }
    }
  }

}