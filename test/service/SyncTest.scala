package service

import scala.concurrent.Future
import org.mockito.Mockito.when
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import client.oauth.OAuthClient
import test.util.MockitoUtils
import org.mockito.Mockito._
import org.mockito.Matchers._
import client.kio.KioClient
import scala.concurrent.Await
import client.oauth.OAuthAccessToken
import scala.concurrent.duration.Duration
/**
 * This class tests the interaction between the Service and the Client(mock).
 */
class SyncTest extends FlatSpec with MockitoSugar with MockitoUtils {

  private val kioClient = mock[KioClient]
  private val oAuthClient = mock[OAuthClient]
  private val store = mock[DataStore]
  private val synchronizer = new SynchronizerImpl(oAuthClient, store, kioClient)

  private val accessToken = createOAuthAccessToken("token_type", "access_token", "scope", 3599)
  private val accessTokenResult = Future.successful { accessToken }
  private val appIds = List("kontrolletti", "cloud-lobster")
  private val appIdsResult = Future.successful(appIds)
  private val apps = List(createAppInfo("scmUrl1", "specUrl1", "docUrl1", "serviceUrl1", "created1", "lastModified1"), //
    createAppInfo("scmUrl2", "specUrl2", "docUrl2", "serviceUrl2", "created2", "lastModified2"))
  private val appsResult = Future.successful(apps)

  "Synchronizer#syncApps" should "store apps from kio in data-store" in {
    when(oAuthClient.accessToken()).thenReturn(accessTokenResult)
    when(kioClient.apps(accessToken)).thenReturn(appsResult)
    when(store.saveAppInfo(apps)).thenReturn(Future.successful(true))

    Await.result(synchronizer.syncApps(), Duration("5 seconds")) match {
      case value: Boolean => assert(value == true)
    }

    verify(oAuthClient, times(1)).accessToken()
    verify(kioClient, times(1)).apps(accessToken)
    verify(store, times(1)).saveAppInfo(apps)
  }

}