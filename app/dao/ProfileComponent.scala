package dao

import slick.jdbc.JdbcProfile

trait ProfileComponent {
  val profile: JdbcProfile
}