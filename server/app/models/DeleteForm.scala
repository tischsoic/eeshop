package models

import play.api.data.Forms._
import play.api.data._

case class DeleteForm(id: Int)
object DeleteForm {
  val deleteForm: Form[DeleteForm] = Form {
    mapping("id" -> number)(DeleteForm.apply)(DeleteForm.unapply)
  }
}
