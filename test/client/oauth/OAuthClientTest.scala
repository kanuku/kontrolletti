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
import configuration.OAuthConfigurationImpl
import configuration.OAuthConfiguration

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
    override def tokenInfoRequestEndpoint = "tokenInfoRequestEndpoint"
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
    when(mockedDispatcher.requestHolder(config.accessTokenRequestEndpoint)).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withHeaders(("Content-Type", "application/x-www-form-urlencoded"))).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withAuth(clientCred.id, clientCred.secret, WSAuthScheme.BASIC)).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withRequestTimeout(config.requestClientTimeout.toLong)).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withQueryString(("grant_type", "password"))).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withQueryString(("username", userCred.username))).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withQueryString(("password", userCred.password))).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withQueryString(("scope", "uid"))).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withQueryString(("realm", "/services"))).thenReturn(mockedRequestHolder)
    implicit val writable = any[Writeable[String]]
    when(mockedRequestHolder.post(anyString)(writable)).thenReturn(mockedResponse)

    Await.result(client.accessToken(), Duration("5 seconds")) match {
      case value: OAuthAccessToken => assert(value === oAuthCred)
      case _                       => fail("Result should not be null");
    }

    verify(mockedRequestHolder, times(1)).post(same(""))(any[Writeable[String]])
    verify(mockedWSResponse, times(1)).body
  }

  "OAuthClient#tokenInfo" should "parse successfull response from tokenEndpoint" in {
    val token = "e27bac64-3d89-4b99-94d4-3bf15e7dada2"
    val tokenInfo = new OAuthTokenInfo("username", Option(List("uid", "cn")), "password", "/employees", "Bearer", 3564, token)
    val requestReponse = """{"uid":"username","scope":["uid","cn"],"grant_type":"password","cn":"","realm":"/employees","token_type":"Bearer","expires_in":3564,"access_token":"e27bac64-3d89-4b99-94d4-3bf15e7dada2"}"""
    val mockedTokenInfoWSResponse = createMockedWSResponse(requestReponse, 200)
    val mockedResponse = Future.successful(mockedTokenInfoWSResponse)

    when(mockedDispatcher.requestHolder(config.tokenInfoRequestEndpoint)).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.withQueryString(("access_token", token))).thenReturn(mockedRequestHolder)
    when(mockedRequestHolder.get()).thenReturn(mockedResponse)

    Await.result(client.tokenInfo(token), Duration("50 seconds")) match {
      case Some(result) => assert(result === tokenInfo, "Result is not equal to expected")
      case None         => fail("Result should not be None");
    }
    verify(mockedTokenInfoWSResponse, times(1)).body
  }

}