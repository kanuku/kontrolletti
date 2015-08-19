package client.kio

import scala.concurrent.Future
import org.mockito.Mockito.when
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import client.oauth.OAuthClient
import service.DataStore
import test.util.MockitoUtils
import service.SynchronizerImpl
import org.mockito.Mockito._
import org.mockito.Matchers._

/**
 * @author fbenjamin
 */
class SynchronizerTest extends FlatSpec with MockitoSugar with MockitoUtils {

  private val oAuthClient = mock[OAuthClient]
  private val kioClient = mock[KioClient]
  private val store = mock[DataStore]
  private val accessToken = Future.successful { createOAuthAccessToken("token_type", "access_token", "scope", 3599) }
  private val appIds = List("kontrolletti", "cloud-lobster")
  private val appIdsResult = Future.successful(appIds)
  private val apps = List(createAppInfo("scmUrl1", "specUrl1", "docUrl1", "serviceUrl1"),
    createAppInfo("scmUrl2", "specUrl2", "docUrl2", "serviceUrl2"))
  private val appsResult = Future.successful(apps)
  private val synchronizer = new SynchronizerImpl(oAuthClient, store, kioClient)

  "Synchronizer#syncApps" should " " in {
    when(oAuthClient.accessToken()).thenReturn(accessToken)
    when(kioClient.appIds()).thenReturn(appIdsResult)
    when(kioClient.apps(appIds)).thenReturn(appsResult)
    when(store.saveAppInfo(apps))
    synchronizer.syncApps()
    
    verify(oAuthClient, times(1)).accessToken()
    verify(kioClient, times(1)).appIds()
    verify(kioClient, times(1)).apps(appIds)
    verify(store, times(1)).saveAppInfo(apps)
  }

}