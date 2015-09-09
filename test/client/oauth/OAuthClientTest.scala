package client.oauth

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import client.RequestDispatcher
import play.api.http.ContentTypeOf
import play.api.http.Writeable
import play.api.libs.ws.WSAuthScheme
import play.api.libs.ws.WSRequest
import play.api.libs.ws.WSResponse
import test.util.MockitoUtils
import test.util.TestUtils._
import utility.Transformer
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * @author fbenjamin
 */
class OAuthClientTest extends FlatSpec with MockitoSugar with MockitoUtils {

  //Contains the same values as in src/test/resources/client.json
  private val clientCred = createOAuthClientCredential("kontrolletti_client_id", "client_secret")
  //Contains the same values as in src/test/resources/user.json
  private val userCred = createOAuthUserCredential("kontrolletti", "password")
  private val oAuthCred = createOAuthAccessToken("token_type", "access_token", "scope", 3599)
  private val mockedDispatcher: RequestDispatcher = mock[RequestDispatcher]
  private val mockedRequestHolder = mock[WSRequest]
  private val oAuthAccessToken = """ {"scope":"scope","expires_in":3599,"token_type":"token_type","access_token":"access_token"}"""
  private val mockedWSResponse = createMockedWSResponse(oAuthAccessToken, 200)
  private val mockedResponse = Future.successful(mockedWSResponse)

  private val config: OAuthConfiguration = new OAuthConfigurationImpl() {
    override def accessTokenRequestEndpoint = "https://auth.server.com/oauth2/access_token"
    override def clientCredentialsFilename = "client.json"
    override def userCredentialsFileName = "user.json"
    override def requestClientTimeout = 8294
    override def credentialsDirectory = {
      this.getClass.getResource("/").getPath
    }
  }

  private val clientImpl: OAuthClientImpl = new OAuthClientImpl(mockedDispatcher, config)
  private val client: OAuth = clientImpl

  "OAuthHelper#parse" should "parse to OAuthClientCredentials" in {
    val input = """{"client_id":"kontrolletti_client_id","client_secret":"client_secret"}"""
    Await.result(Transformer.parse2Future(input).flatMap(Transformer.deserialize2Future(_)(OAuthParser.oAuthClientCredentialReader)), Duration("5 seconds")) match {
      case clientCredentials: OAuthClientCredential => assert(clientCredentials === clientCred)
      case _                                        => fail("Result should not be null");
    }
  }
  it should "parse OAuthAccessToken" in {
    Await.result(Transformer.parse2Future(oAuthAccessToken).flatMap(Transformer.deserialize2Future(_)(OAuthParser.oAuthAccessTokenReader)), Duration("5 seconds")) match {
      case accessTokenCredentials: OAuthAccessToken => assert(accessTokenCredentials === oAuthCred)
      case _                                        => fail("Result should not be null");
    }
  }

  "OAuthClient#clientCredentials" should "return client credentials" in {
    Await.result(client.clientCredentials(), Duration("5 seconds")) match {
      case clientCredentials: OAuthClientCredential => assert(clientCredentials === clientCred)
      case _                                        => fail("Result should not be null");
    }
  }

  "OAuthClient#userCredentials" should "return user credentials" in {
    Await.result(client.userCredentials(), Duration("5 seconds")) match {
      case userCredentials: OAuthUserCredential => assert(userCredentials === userCred)
      case _                                    => fail("Result should not be null");
    }
  }

  "OAuthClient#accessToken" should "return access-token" in {
    val body = s"grant_type=password&scope=uid&username=$userCred.username&password=$userCred.password"
    when(mockedDispatcher.requestHolder(anyString)).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withHeaders(any())).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withAuth(anyString, anyString, any())).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withRequestTimeout(anyInt)).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withQueryString(any())).thenReturn(mockedRequestHolder)
    implicit val writable = any[Writeable[String]]
    when(mockedRequestHolder.post(anyString)(writable)).thenReturn(mockedResponse)

    Await.result(client.accessToken(), Duration("5 seconds")) match {
      case value: OAuthAccessToken => assert(value === oAuthCred)
      case _                       => fail("Result should not be null");
    }

    verify(mockedDispatcher, times(1)).requestHolder(config.accessTokenRequestEndpoint)
    verify(mockedRequestHolder, times(1)).withHeaders(("Content-Type", "application/x-www-form-urlencoded"))
    verify(mockedRequestHolder, times(1)).withAuth(clientCred.id, clientCred.secret, WSAuthScheme.BASIC)
    verify(mockedRequestHolder, times(1)).withRequestTimeout(config.requestClientTimeout)
    verify(mockedRequestHolder, times(1)).withQueryString(("realm", "/services"))
    verify(mockedRequestHolder, times(1)).withQueryString(("grant_type", "password"))
    verify(mockedRequestHolder, times(1)).withQueryString(("grant_type", "password"))
    verify(mockedRequestHolder, times(1)).withQueryString(("grant_type", "password"))
    verify(mockedRequestHolder, times(1)).withQueryString(("grant_type", "password"))
    verify(mockedRequestHolder, times(1)).withQueryString(("grant_type", "password"))
    verify(mockedRequestHolder, times(1)).post(same(""))(any[Writeable[String]])
    verify(mockedWSResponse, times(1)).body

  }

}