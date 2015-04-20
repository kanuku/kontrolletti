package v1.service

import javax.inject._
import v1.client.SCMClient
import v1.model.Commit
import v1.model.Repository
import v1.model.User

trait SearchService {

  def split(url: String): (String, String, String)
  def users: List[User]
  def user(url: String): User
  def users(url: String): List[User]

  def repos: List[Repository]
  def repo(url: String): Repository
  def repos(url: String): List[Repository]

  def commits: List[Commit]
  def commit(url: String): Commit
  def commits(url: String): List[Commit]
  def commitsSince(url: String): List[Commit]
  def commitsUtil(url: String): List[Commit]

}

@Singleton
class SearchServiceImpl @Inject() (scmClient: SCMClient) extends SearchService {
  def split(url: String): (String, String, String) = ???

  def users: List[User] = ???
  def user(url: String): User = ???
  def users(url: String): List[User] = ???

  def repos: List[Repository] = ???
  def repo(url: String): Repository = ???
  def repos(url: String): List[Repository] = ???

  def commits: List[Commit] = ???
  def commit(url: String): Commit = ???
  def commits(url: String): List[Commit] = ???
  def commitsSince(url: String): List[Commit] = ???
  def commitsUtil(url: String): List[Commit] = ???

}

class FakeSearchServiceImpl extends SearchService {
  def split(url: String): (String, String, String) = ???
  def user(url: String): User = ???
  def users: List[User] = ???
  def users(url: String): List[User] = ???

  def repo(url: String): Repository = ???
  def repos: List[Repository] = ???
  def repos(url: String): List[Repository] = ???

  
  def commits: List[Commit] = ???
  def commit(url: String): Commit = ???
  def commits(url: String): List[Commit] = ???
  def commitsSince(url: String): List[Commit] = ???
  def commitsUtil(url: String): List[Commit] = ???

}