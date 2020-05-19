package dao

import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcBackend.Database
import slick.lifted.AbstractTable

import scala.concurrent.{ExecutionContext, Future}

abstract class Repository[T <: AbstractTable[_], I: BaseTypedType](val profile: JdbcProfile, val db: Database) {
  import profile.api._

  type Id = I
  def table: TableQuery[T]

  def getId(row: T): Rep[Id]
  def setId(model: T#TableElementType, id: Id): T#TableElementType

  def all() = db.run(table.result)

  def filterById(id: Id) = table filter (getId(_) === id)
  def findById(id: Id) = db run filterById(id).result.headOption

  def insert(model: T#TableElementType) = db run (table += model)

  def insertWithReturn(model: T#TableElementType): Future[T#TableElementType] = {
    // Ref https://stackoverflow.com/questions/31443505/slick-3-0-insert-and-then-get-auto-increment-value
    val insertQuery = table returning table.map(getId) into setId
    db.run(insertQuery += model)
  }

  def update(id: Id, model: T#TableElementType) = {
    db.run(table.filter(getId(_) === id).update(model))
  }

  def deleteById(id: Id) = {

    val deleteAction = buildDeleteAction(id)
    db run deleteAction.delete
  }

  private def buildDeleteAction(id: Id) = {
    profile.createDeleteActionExtensionMethods(
      profile.deleteCompiler.run(filterById(id).toNode).tree, ()
    )
  }
}
