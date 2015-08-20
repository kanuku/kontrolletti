package service

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
import client.kio.KioClient
/**
 * This class tests the interaction between the Service and the Client(mock).
 */
class SynchronizerTest extends FlatSpec with MockitoSugar with MockitoUtils {

  private val oAuthClient = mock[OAuthClient]
  private val kioClient = mock[KioClient]
  private val store = mock[DataStore]
  private val accessToken = createOAuthAccessToken("token_type", "access_token", "scope", 3599)
  private val accessTokenResult = Future.successful { accessToken }
  private val appIds = List("kontrolletti", "cloud-lobster")
  private val appIdsResult = Future.successful(appIds)
  private val apps = List(createAppInfo("scmUrl1", "specUrl1", "docUrl1", "serviceUrl1"), createAppInfo("scmUrl2", "specUrl2", "docUrl2", "serviceUrl2"))
  private val appsResult = Future.successful(apps)
  private val synchronizer = new SynchronizerImpl(oAuthClient, store, kioClient)

  "Synchronizer#syncApps" should "store apps from kio in data-store" in {
    when(oAuthClient.accessToken()).thenReturn(accessTokenResult)
    when(kioClient.apps(accessToken)).thenReturn(appsResult)
    synchronizer.syncApps()
    verify(oAuthClient, times(1)).accessToken()
    verify(kioClient, times(1)).apps(accessToken)
    verify(store, times(1)).saveAppInfo(apps)
  }

}