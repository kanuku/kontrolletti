package service

import javax.inject.Singleton
import model.AppInfo
import model.Commit
import scala.concurrent.Future
/**
 * @author fbenjamin
 */

trait DataStore {

  def saveAppInfo(apps: List[AppInfo]): Future[Boolean]
  
  def saveCommits(commits: List[Commit]): Future[Boolean]

  def scmUrls(): Future[List[String]]

}

@Singleton
class DataStoreImpl extends DataStore {

  def saveAppInfo(app: List[AppInfo]): Future[Boolean] = ???

  def scmUrls(): Future[List[String]] = ???
  
  def saveCommits(commits: List[Commit]): Future[Boolean] = ???

}