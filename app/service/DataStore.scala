package service

/**
 * @author fbenjamin
 */

trait DataStore {

  def saveRepos(host: String, project: String, repository: String) = ???

}

class DataStoreImpl {
  
   def saveRepos(host: String, project: String, repository: String) = ???

}