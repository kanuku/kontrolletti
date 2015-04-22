package v1.client

import v1.model.Repository
import v1.model.Commit
import v1.model.Resource
import v1.model.User
import scala.concurrent.Future
import play.api.libs.ws.WSResponse

trait SCM{

  def name: String

  //  def resources: String
  //  def resource(name: String): String
  //
  //  def users: String
      def contributorsByRepo(group:String, repo: String): Future[WSResponse]
  //  def user(name: String, resource: String): String
  //
  //  def commits: String
  //  def commits(id: String): String
  //  def commit(id: String, resource: String): String
  //
  //  def repositories: String
  //  def repositories(name: String): String
  //  def repository(name: String, resource: String): String

}
trait SearchClient {

  def name: String
  def resources: List[Resource]
  def resource(name: String): Resource

  def users: List[User]
  def users(name: String): List[User]
  def user(name: String, resource: String): User

  def commits: List[Commit]
  def commits(id: String): List[Commit]
  def commit(id: String, resource: String): Commit

  def repositories: List[Repository]
  def repositories(name: String): List[Repository]
  def repository(name: String, resource: String): Repository

}

 