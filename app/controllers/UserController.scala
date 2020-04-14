package controllers

import javax.inject._
import models.{User, UserRole}
import slick.jdbc.JdbcBackend.Database
//import models.User
import play.api.mvc._

import scala.concurrent.ExecutionContext

//object SQLitePersistence @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
//   {
//    val profile = profile
//    val db = db
//}

//object SQLiteUserComponent
//  extends UserComponent, SQLitePersistence
//    with SQLitePersistence


//trait SQLitePersistence
//  extends ProfileComponent
//    with DatabaseComponent {
//  val profile = slick.jdbc.SQLiteProfile
//  val db = Database.forConfig("default")
//}
//
//object SQLiteUserComponent
//  extends UserComponent
//    with SQLitePersistence



@Singleton
class UserController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteUserComponent._
  import dao.SQLiteUserComponent.profile.api._

  def index = Action.async {
//    val db1 = slick.jdbc.JdbcBackend.Database.forConfig("default")
//    val db2 = slick.jdbc.JdbcBackend.Database.forConfig("slick.dbs.default.db")
    UserRepository.all().map(users => Ok(users.map(_.toString).mkString(" ;;; ")))
  }

  def addUser(e: String) = Action.async {
    val newUser = User(0, e + "a@a.com", "Jfstname", "B", "pass", UserRole.Staff)
    UserRepository.insertWithReturn(newUser).map(user => Ok(s"user with id=${user.userId} added!"))
  }

  def getUserById(id: Int) = Action.async {
    UserRepository.findById(id).map {
      case None => NotFound("User not found!")
      case Some(user) => Ok(user.toString)
    }
  }

}
