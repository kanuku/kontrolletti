package v1.client

import v1.model.Repository
import v1.model.Commit
import v1.model.Resource
import v1.model.User

trait Client {

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

/**
 * This client provides some dummy data
 */
object Clients extends Client {

  private val resourcesList: List[Resource] = List(
    Resource("stash", "https://stash.zalando.net"),
    Resource("github", "https://github.com"),
    Resource("github-enterprise", "https://ghe.cd.aws.zalando.net"))

  private val commitsList: List[Commit] = List(
    Commit("a162117sasd8q96wq55sdhh", "CD-1347", User("Peter Janssen", "peterJanssen@gmail.com")),
    Commit("182931627sasd8q96we5qas891", "CD-2354", User("Peter Janssen", "peterJanssen@gmail.com")),
    Commit("18293g7fd5a7765", "CD-9954", User("Peter Janssen", "peterJanssen@gmail.com")),
    Commit("716623ahskj15274jk", "CD-6693", User("Peter Janssen", "peterJanssen@gmail.com")))

  private val usersList: List[User] = List(
    User("Peter Janssen", "peterJanssen@gmail.com"),
    User("Jassenn Peter", "Janssenpeter@gmail.com"),
    User("Lothar Schulz", "Schulzie@zalando.de"))

  private val repositoriesList: List[Repository] =
    List(
      Repository("dockerfiles", resourcesList(0), "https://stash.zalando.net/projects/DOC",
        List(commitsList(2), commitsList(1))),
      Repository("dockerfiles", resourcesList(1), "http://github.com/zalando-bus/kontrolletti",
        List(commitsList(0), commitsList(1))),
      Repository("zalando-automata", resourcesList(1), "http://github.com/zalando-bus/play-swagger-ui",
        List(commitsList(2), commitsList(3))))

  def name = "dummyClient"
  // List functions
  def user(name: String, resource: String): User = {
    	//One-liner: 
    	repositoriesList.filter(x => x.resource.name == resource) // Find resource by name
    	.flatMap { x => x.commits } // Then get commits from repos
    	.map { x => x.committer } // Then get users from commits
    	.find { x => x.name == name } // Filter users by name
    	.getOrElse(null)
    }
  def users = usersList
  def users(name: String): List[User] = usersList.filter { x => x.name==name }

  def commit(id: String, resource: String): Commit = {
    def commits = commitsList
    commitsList.find(x => x.id == id).getOrElse(null)
  }
  def commits = commitsList
  def commits(id: String): List[Commit] = {
    commitsList.filter(x => x.id == id)
  }

  def repositories = repositoriesList
  def repositories(name: String): List[Repository] = {
    repositoriesList.filter(x => x.name == name)
  }

  def repository(name: String, resource: String): Repository = {
    repositoriesList.find(x => x.name == name && x.resource.name == resource).getOrElse(null)
  }

  def resources = resourcesList

  def resource(name: String): Resource = {
    resourcesList.find { x => x.name == name }.getOrElse(null)
  }

  def committer(name: String, resource: String): User = {
    usersList.find { x => x.name == name }.getOrElse(null)

  }
} 