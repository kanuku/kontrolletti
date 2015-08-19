package service

import javax.inject.Singleton
import model.AppInfo
/**
 * @author fbenjamin
 */

trait DataStore {

  def saveAppInfo(apps: List[AppInfo])

}

@Singleton
class DataStoreImpl extends DataStore{

  def saveAppInfo(app: List[AppInfo]) = ???

}