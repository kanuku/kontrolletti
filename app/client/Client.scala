package client

import model._

object Client {

  private val commitsList: List[Commit] = List(
    Commit("a162117sasd8q96wq55sdhh", "CD-1347", Committer("Peter Janssen", "peterJanssen@gmail.com")),
    Commit("182931627sasd8q96we5qas891", "CD-2354", Committer("Peter Janssen", "peterJanssen@gmail.com")),
    Commit("18293g7fd5a7765", "CD-9954", Committer("Peter Janssen", "peterJanssen@gmail.com")),
    Commit("716623ahskj15274jk", "CD-6693", Committer("Peter Janssen", "peterJanssen@gmail.com")))

  private val allCommitters: List[Committer] = List(
    Committer("Peter Janssen", "peterJanssen@gmail.com"),
    Committer("Jassenn Peter", "Janssenpeter@gmail.com"))

  //This is just some dummy data
  private var repositoriesList: List[Repository] = {
    List(
      Repository("zalando-bus",
        List(commitsList(0), commitsList(1))),
      Repository("zalando-automata",
        List(commitsList(2), commitsList(3))))
  }
  def committers = allCommitters
  def repositories = repositoriesList
  def commits = commitsList
} 