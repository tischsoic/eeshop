package dao

import javax.inject.Inject
import models.FaqNote
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class FaqNotesDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends Dao {
  import profile.api._

  def all(): Future[Seq[FaqNote]] = db.run(FaqNotes.result)

  def insert(faqNote: FaqNote): Future[FaqNote] = {
    val insertQuery = FaqNotes returning FaqNotes.map(_.faqNoteId) into ((faqNote, id) => faqNote.copy(faqNoteId = id))
    db.run(insertQuery += faqNote)
  }

}

