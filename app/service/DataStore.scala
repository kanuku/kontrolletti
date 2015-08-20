package service

import javax.inject.Singleton
import model.AppInfo
import scala.concurrent.Future
/**
 * @author fbenjamin
 */

trait DataStore {

  def saveAppInfo(apps: List[AppInfo]):Future[Boolean]

}

@Singleton
class DataStoreImpl extends DataStore{

  def saveAppInfo(app: List[AppInfo]):Future[Boolean] = ???

}