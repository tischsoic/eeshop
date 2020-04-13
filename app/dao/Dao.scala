package dao

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

// Ref: https://stackoverflow.com/questions/52624343/how-to-extract-slick-entities-from-play-framework-dao-singleton
trait Dao extends HasDatabaseConfigProvider[JdbcProfile] with Tables
