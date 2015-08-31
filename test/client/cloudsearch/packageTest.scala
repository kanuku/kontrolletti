package client.cloudsearch

import org.scalatest.mock.MockitoSugar
import org.scalatest.FlatSpec
import model.AppInfo
import model._
import model._
import play.api.libs.json.Json
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import test.util.FakeResponseData
import play.api.libs.json.JsResult
import test.util.MockitoUtils
/**
 * @author fbenjamin
 */
class PackageTest extends FlatSpec with MockitoSugar with MockitoUtils {

  val appInfo1 = new AppInfo("scmUrl1", "serviceUrl1", "created1", "lastModified1")
  val appInfo2 = new AppInfo("scmUrl2", "serviceUrl2", "created2", "lastModified2")
  val author = createAuthor("name1", "email1", List())
  val link1 = createLink("href1", "method1", "rel1", "relType1")
  val link2 = createLink("href2", "method2", "rel2", "relType2")
  val commit1 = createCommit("id1", "message1", List("parentId1"), author, Option(true), List(link1))
  val commit2 = createCommit("id2", "message2", List("parentId2"), author, Option(true), List(link2))

  "package.utils#app2Id" should "transform an AppInfo.scmUrl to an [id]" in {
    val result = app2Id(appInfo1)
    assert(result == "scmUrl1")
  }
  "package.util#commit2Id" should "transform an Commit ton an [id]" in {
    val result = commit2Id(appInfo1)(commit1)
    assert(result == "scmUrl1-id1")
  }
  "package.utils#transform" should "transform list of AppInfos to list of UploadDocuments" in {
    val apps = List(appInfo1, appInfo2)
    val result = transform2UploadRequest(apps, "add")
    val r1 = result(0)
    val r2 = result(1)
    assert(r1.document == appInfo1)
    assert(r1.id == "scmUrl1")
    assert(r1.operation == "add")
    assert(r2.document == appInfo2)
    assert(r2.id == "scmUrl2")
    assert(r2.operation == "add")
  }
  it should "transform json to a appInfoResponse" in {
    Json.fromJson[AppInfoResponse](Json.parse(FakeResponseData.cloudSearchAppsResult)) match {
      case s: JsSuccess[AppInfoResponse] =>

        val result = s.get
        assert(result.found == 210)
        assert(result.start == 0)
        assert(result.result.isDefined)

        val Some(appInfos) = result.result
        val appInfo1 = appInfos(0)
        val appInfo2 = appInfos(1)
        val appInfo3 = appInfos(2)

        assert(appInfo1.documentationUrl == "serviceUrl-0")
        assert(appInfo1.scmUrl == "scmUrl-0")
        assert(appInfo1.specificationUrl == "created-0")
        assert(appInfo1.lastModified == "2015-08-25T07:48:53.557Z")

        assert(appInfo2.documentationUrl == "serviceUrl-1")
        assert(appInfo2.scmUrl == "scmUrl-1")
        assert(appInfo2.specificationUrl == "created-1")
        assert(appInfo2.lastModified == "2015-08-25T07:48:53.557Z")
        assert(appInfo3.documentationUrl == "serviceUrl-2")
        assert(appInfo3.scmUrl == "scmUrl-2")
        assert(appInfo3.specificationUrl == "created-2")
        assert(appInfo3.lastModified == "2015-08-25T07:48:53.557Z")

      case e: JsError => fail("It should parse without problems!")
    }
  }
  it should "transform to a list of CommitResponse" in {
    implicit val appInfo = commit2Id(appInfo1)
    val result = transform2UploadRequest(List(commit1, commit2), "add")
    val c1 = result(0)
    val c2 = result(1)
    assert(c1.id == "scmUrl1-id1")
    assert(c1.document === commit1)
    assert(c1.operation == "add")
    assert(c2.id == "scmUrl1-id2")
    assert(c2.document === commit2)
    assert(c2.operation == "add")

  }

  "package.utils#uploadDocumentWrites" should "parse parse a UploadDocument" in {
    val app = new AppInfo("scmUrl", "serviceUrl", "created", "lastModified")
    val document = new UploadRequest("id", "add", app)
    val json = Json.toJson(document)
    implicit val uploadDocumentAppInfoFormat = uploadRequestFormat[UploadRequest[AppInfo]]
    Json.fromJson[UploadRequest[AppInfo]](Json.toJson(document)) match {
      case JsSuccess(result, _) =>
        assert(result === document)
      case e: JsError =>
        fail("Should not result in an error")

    }
  }

}