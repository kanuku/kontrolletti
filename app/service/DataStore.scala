package service

import javax.inject.Singleton
import model.AppInfo
import model.Commit
import scala.concurrent.Future
import com.google.inject.ImplementedBy
import javax.inject.Inject
import dao.AppInfoRepository
/**
 * @author fbenjamin
 */
trait DataStore {


  def saveCommits(commits: List[Commit]): Future[Boolean]

  def scmUrls(): Future[List[String]]

}

@Singleton
class DataStoreImpl @Inject() (appRepo: AppInfoRepository) extends DataStore {

  def scmUrls(): Future[List[String]] = Future.successful(List())

  def saveCommits(commits: List[Commit]): Future[Boolean] = Future.successful(true)

}