package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import models.DeleteForm.deleteForm
import models.FaqNote
import models.services.AuthenticateService
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._
import utils.DefaultEnv

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

  def getFaqNotes = silhouette.SecuredAction.async {
    FaqNotesRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def index = Action.async {
    FaqNotesRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newFaqNote = FaqNote(0, "title", "message")
    FaqNotesRepository.insertWithReturn(newFaqNote).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    FaqNotesRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

//  def update(id: Int) = Action.async {
//    FaqNotesRepository.update(id, FaqNote(id, "title", "message")).map(r => Ok(Json.toJson(r)))
//  }

}
