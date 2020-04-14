package dao

//object SQLiteFaqNotesComponent
//  extends FaqNotesComponent
//    with SQLitePersistence

//trait FaqNotesComponent { this: DatabaseComponent with ProfileComponent =>
//
//  import slick.lifted.Tag
//  import profile.api._
//
//  case class FaqNote(faqNoteId: Int, title: String, message: String)
//
//  class FaqNotesTable(tag: Tag) extends Table[FaqNote](tag, "faq_notes") {
//    def faqNoteId = column[Int]("faq_note_id", O.PrimaryKey, O.AutoInc)
//    def title = column[String]("order_id")
//    def message = column[String]("message")
//
//    def * = (faqNoteId, title, message) <> (FaqNote.tupled, FaqNote.unapply)
//  }
//
//  object FaqNotesRepository extends Repository[FaqNotesTable, Int](profile, db) {
//    import this.profile.api._
//
//    val table = TableQuery[FaqNotesTable]
//    def getId(table: FaqNotesTable) = table.faqNoteId
//    def setId(user: FaqNote, id: Id) = user.copy(faqNoteId = id)
//  }
//
//}

