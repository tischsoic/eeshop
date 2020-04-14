package controllers

import javax.inject._
import models.FaqNote
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class FaqNoteController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteFaqNotesComponent._

  def index = Action.async {
    FaqNotesRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newUser = FaqNote(0, "title", "message")
    FaqNotesRepository.insertWithReturn(newUser).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    FaqNotesRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

  def update(id: Int) = Action.async {
    FaqNotesRepository.update(id, FaqNote(id, "title", "message")).map(r => Ok(Json.toJson(r)))
  }

}
