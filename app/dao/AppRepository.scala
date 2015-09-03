package dao

/**
 * @author fbenjamin
 */



import scala.concurrent.{Future, ExecutionContext}
import play.api.db.slick.DatabaseConfigProvider
import javax.inject.{ Inject, Singleton }
import slick.driver.JdbcProfile
import dao.model.Apps

/**
 * Created by format on 15/8/21.
 */
@Singleton
class AppsRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import driver.api._

  private class AppsTable(tag: Tag) extends Table[Apps](tag, "students") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def age = column[Int]("age")

    def * = (id, name, age) <> ((Apps.apply _).tupled, Apps.unapply)
  }

  private val apps = TableQuery[AppsTable]


  def create(name: String, age: Int): Future[Apps] = db.run {
    (apps.map(s => (s.name, s.age))
      returning apps.map(_.id)
      into ((nameAge, id) => Apps(id, nameAge._1, nameAge._2))
      ) += (name, age)
  }


  def list(): Future[Seq[Apps]] = db.run {
    apps.result
  }

}