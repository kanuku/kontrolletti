package elasticsearch

import elasticsearch.model._

object Client {

  
  //This is just some dummy data
  private var list: List[Repository] = {
    List(
      Repository("zalando-bus",
        List(
          Commit("a162117sasd8q96wq55sdhh", "CD-1347", Author("Peter Janssen", "peterJanssen@gmail.com")),
          Commit("182931627sasd8q96we5qas891", "CD-2354", Author("Peter Janssen", "peterJanssen@gmail.com")))),
      Repository("zalando-automata",
        List(
          Commit("18293g7fd5a7765", "CD-9954", Author("Peter Janssen", "peterJanssen@gmail.com")),
          Commit("716623ahskj15274jk", "CD-6693", Author("Peter Janssen", "peterJanssen@gmail.com")))))
  }

  def repositories = list
} 