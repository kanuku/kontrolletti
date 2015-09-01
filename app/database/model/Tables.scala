package database.model
//import scala.concurrent.Await
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.duration.Duration
//import model.Commit
//import MyPostgresDriver.api._
//import model.Author
//import slick.lifted.ProvenShape
//import com.github.tminglei.slickpg._
///**
// * @author fbenjamin
// */
//
//class Authors(tag: Tag) extends Table[Author](tag, "AUTHORS") {
//  def email = column[String]("email", O.PrimaryKey)
//  def payload = column[String]("payload")
//  def * : ProvenShape[(String, String)] = (email, payload)
//}
//
//////case class Commit(id:String,name:String)
////class Commits(tag: Tag) extends Table[(String, List[String], String)](tag, "COMMITS") {
////  def id = column[String]("id", O.PrimaryKey)
////  def parentIds = column[List[String]]("parent_ids")
////  def payload = column[String]("payload")
////  def * : ProvenShape[(String, List[String], String)] = (id, parentIds, payload)
////}
//
//
//
//object Test extends App {
//  
//  println("#####")
//    val db = Database.forURL("jdbc:postgresql://localhost:5432/kontrolletti;DB_CLOSE_DELAY=-1", driver="org.postgresql.Driver")
//    println("#####")
//  
//  
//  
//}