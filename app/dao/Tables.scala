package dao
//import scala.concurrent.Await
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.duration.Duration
//import model._
//import MyPostgresDriver.api._
//import model.Author
//import slick.lifted.ProvenShape
//import com.github.tminglei.slickpg._
//import model.AppInfo
///**
// * @author fbenjamin
// */
//
//class AppInfos(tag: Tag) extends Table[AppInfo](tag, "APP_INFO") {
//  def scmUrl = column[String]("scm_url", O.PrimaryKey)
//  def documentationUrl = column[String]("scm_url")
//  def specificationUrl = column[String]("spec_url")
//  def lastModified = column[String]("last_mod")
//
//  def * = (scmUrl, documentationUrl, specificationUrl, lastModified) <> (AppInfo.tupled, AppInfo.unapply _)
//
//}
// 
//object Main {
//
//   
//
//  def main(args: Array[String]): Unit = {
//  println("#####")
//  val apps = TableQuery[AppInfos]
//  
//  val connectionUrl = "jdbc:postgresql://localhost/my-db?user=postgres&password=postgres"
//  
//  val db = Database.forURL(connectionUrl, driver = "org.postgresql.Driver")
//  
//  def saveAppInfos(input: Seq[AppInfo]) = db.run(apps ++= input).map(_ => ())
//  
//  
//  
//  
//  println("#####")
//    // my database server is located on the localhost
//    // database name is "my-db"
//    // username is "postgres"
//    // and password is "postgres"
//
//    Database.forURL(connectionUrl, driver = "org.postgresql.Driver") withSession {
//      implicit session =>
//        val users = TableQuery[Users]
//
//        // SELECT * FROM users
//        users.list foreach { row =>
//          println("user with id " + row._1 + " has username " + row._2)
//        }
//
//        // SELECT * FROM users WHERE username='john'
//        users.filter(_.username === "john").list foreach { row => 
//           println("user whose username is 'john' has id "+row._1 )
//        }
//    }
//  }
//}