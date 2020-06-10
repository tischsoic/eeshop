package dao

import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.reflect.ClassTag

abstract class AuthRepository(val profile: JdbcProfile, val db: Database) {
  import profile.api._
}
