package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import models.DeleteForm.deleteForm
import models.{FaqNote, UserRole}
import models.services.AuthenticateService
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import utils.DefaultEnv
import utils.auth.HasRole

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FaqNoteController @Inject()(
                                   silhouette: Silhouette[DefaultEnv],
                                   authenticateService: AuthenticateService,
                                   cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import dao.SQLiteFaqNotesComponent._

//  case class FaqNote(faqNoteId: Int, title: String, message: String)
  val faqNoteForm: Form[FaqNote] = Form {
    mapping(
      "faqNoteId" -> default(number, 0),
      "title" -> nonEmptyText,
      "message" -> nonEmptyText,
    )(FaqNote.apply)(FaqNote.unapply)
  }

  def create() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.faqNote.create(faqNoteForm))
  }

  def handleCreate() = Action.async { implicit request =>
    faqNoteForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(
          BadRequest(views.html.faqNote.create(formWithErrors))
        )
      },
      faqNote => {
        FaqNotesRepository.insertWithReturn(faqNote).map(
          _ => Redirect(routes.FaqNoteController.create()).flashing("success" -> "faqNote.created")
        )
      }
    )
  }

  def update(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    FaqNotesRepository.findById(id).map {
      case None => NotFound(Json.obj({"error" -> "FaqNote not found"}))
      case Some(faqNote) => Ok(views.html.faqNote.update(faqNoteForm.fill(faqNote)))
    }
  }

  def handleUpdate() = Action.async { implicit request =>
    faqNoteForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(
          BadRequest(views.html.faqNote.update(formWithErrors))
        )
      },
      faqNote => {
        FaqNotesRepository.update(faqNote.faqNoteId, faqNote).map(
          _ => Redirect(routes.FaqNoteController.update(faqNote.faqNoteId)).flashing("success" -> "faqNote.updated")
        )
      }
    )
  }

  def all = Action.async { implicit request: MessagesRequest[AnyContent] =>
    FaqNotesRepository.all().map(faqNotes => Ok(views.html.faqNote.all(faqNotes, deleteForm)))
  }

  def handleDelete() = Action.async { implicit request =>
    deleteForm.bindFromRequest.fold(
      _ => {
        FaqNotesRepository.all().map(faqNotes => BadRequest(views.html.faqNote.all(faqNotes, deleteForm)))
      },
      form => {
        FaqNotesRepository.deleteById(form.id).map(
          _ => Redirect(routes.FaqNoteController.all()).flashing("success" -> "faqNote.deleted")
        )
      }
    )
  }

  /////////////////////////////////////////////////////////////////

  def create_REST =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      val title = (request.body \ "title").as[String]
      val message = (request.body \ "message").as[String]

      FaqNotesRepository.insertWithReturn(FaqNote(0, title, message)).map(faqNote => Ok(Json.toJson(faqNote)))
    }

  def read_REST(id: Int) =
    Action.async { implicit request: Request[AnyContent] =>
      FaqNotesRepository.findById(id).map(faqNote => Ok(Json.toJson(faqNote)))
    }

  def readAll_REST =
    Action.async { implicit request: Request[AnyContent] =>
      FaqNotesRepository.all().map(faqNotes => Ok(Json.toJson(faqNotes)))
    }

  def update_REST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      val title = (request.body \ "title").as[String]
      val message = (request.body \ "message").as[String]

      FaqNotesRepository.update(id, FaqNote(id, title, message)).map(_ => Accepted)
    }

  def delete_REST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      FaqNotesRepository.deleteById(id).map(_ => Accepted)
    }

}
