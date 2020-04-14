package dao

import slick.jdbc.JdbcBackend.Database

trait SQLitePersistence
  extends ProfileComponent
    with DatabaseComponent {
  val profile = slick.jdbc.SQLiteProfile
  val db = Database.forConfig("slick.dbs.default.db")
}

//object SQLiteUserComponent
//  extends UserComponent
//    with SQLitePersistence