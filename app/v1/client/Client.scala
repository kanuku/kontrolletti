package v1.client

import v1.model.Repository
import v1.model.Committer
import v1.model.Commit
import v1.model.Resource

object Client {

  private val resourcesList: List[Resource] = List(
    Resource("stash", "https://stash.zalando.net"),
    Resource("github", "https://github.com"),
    Resource("github-enterprise", "https://ghe.cd.aws.zalando.net"))

  private val commitsList: List[Commit] = List(
    Commit("a162117sasd8q96wq55sdhh", "CD-1347", Committer("Peter Janssen", "peterJanssen@gmail.com")),
    Commit("182931627sasd8q96we5qas891", "CD-2354", Committer("Peter Janssen", "peterJanssen@gmail.com")),
    Commit("18293g7fd5a7765", "CD-9954", Committer("Peter Janssen", "peterJanssen@gmail.com")),
    Commit("716623ahskj15274jk", "CD-6693", Committer("Peter Janssen", "peterJanssen@gmail.com")))

  private val committersList: List[Committer] = List(
    Committer("Peter Janssen", "peterJanssen@gmail.com"),
    Committer("Jassenn Peter", "Janssenpeter@gmail.com"))

  //This is just some dummy data
  private val repositoriesList: List[Repository] =
    List(
      Repository("dockerfiles", resourcesList(0), "https://stash.zalando.net/projects/DOC",
        List(commitsList(2), commitsList(1))),
      Repository("dockerfiles", resourcesList(1), "http://github.com/zalando-bus/kontrolletti",
        List(commitsList(0), commitsList(1))),
      Repository("zalando-automata", resourcesList(1), "http://github.com/zalando-bus/play-swagger-ui",
        List(commitsList(2), commitsList(3))))

  // List functions
  def committers = committersList
  def repositories = repositoriesList
  def commits = commitsList
  def resources = resourcesList

  // Query functions
  def commit(id: String, resource: String): List[Commit] = {
    commits.filter(x => x.id == id)
  }
  def repository(name: String): List[Repository] = {
    repositoriesList.filter(x => x.name == name)
  }
  def repository(name: String, resource: String): List[Repository] = {
    repositoriesList.filter(x => x.name == name && x.resource.name == resource)
  }

  def resource(name: String): Resource = {
    resourcesList.find { x => x.name == name }.getOrElse(null)
  }

  def committer(name: String, resource:String): Committer = {
    committersList.find { x => x.name == name }.getOrElse(null)

  }
} 