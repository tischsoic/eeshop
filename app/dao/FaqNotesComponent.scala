package dao

import models.FaqNote

object SQLiteFaqNotesComponent
  extends FaqNotesComponent
    with SQLitePersistence

trait FaqNotesComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object FaqNotesRepository extends Repository[FaqNotesTable, Int](profile, db) {
    val table = FaqNotes
    def getId(table: FaqNotesTable) = table.faqNoteId
    def setId(product: FaqNote, id: Id) = product.copy(faqNoteId = id)
  }
}

