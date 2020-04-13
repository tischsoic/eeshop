package dao

import javax.inject.Inject
import models.User
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class UsersDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends Dao {
  import profile.api._

  def all(): Future[Seq[User]] = db.run(Users.result)

  def getById(userId: Int): Future[Option[User]] = db.run(Users.filter(_.userId === userId).result.headOption)

  def insert(user: User): Future[User] = {
    val insertQuery = Users returning Users.map(_.userId) into ((user, id) => user.copy(userId = id))
    db.run(insertQuery += user)
  }

  // TODO: any generic solution ???
//  def insert2[Element <: Object](
//                        element: Element,
//                        Elements: TableQuery[Table[Element]],
//                        getId: (Table[Element]) => Int,
//                        setId: (Element, Int) => Element): Future[Element] = {
//    val insertQuery = Elements returning Elements.map(getId) into ((user, id) => setId(user.copy, id))
//    db.run(insertQuery += element)
//  }

}

