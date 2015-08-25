package client.cloudsearch

import org.scalatest.mock.MockitoSugar
import org.scalatest.FlatSpec
import model.AppInfo
import model._
import model._
import play.api.libs.json.Json
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
/**
 * @author fbenjamin
 */
class packageTest extends FlatSpec with MockitoSugar {

  "package.utils#app2Id" should "transform an AppInfo.scmUrl to an [id]" in {
    val appInfo1 = new AppInfo("scmUrl1", "serviceUrl1", "created1", "lastModified1")
    val result = app2Id(appInfo1)
    assert(result == "scmUrl1")
  }

  "package.utils#transform" should "transform list of AppInfos to list of UploadDocuments to Post" in {
    val appInfo1 = new AppInfo("scmUrl1", "serviceUrl1", "created1", "lastModified1")
    val appInfo2 = new AppInfo("scmUrl2", "serviceUrl2", "created2", "lastModified2")
    val apps = List(appInfo1, appInfo2)
    val result = transform(apps, "add")
    val r1 = result(0)
    val r2 = result(1)
    assert(r1.document == appInfo1)
    assert(r1.id == "scmUrl1")
    assert(r1.operation == "add")
    assert(r2.document == appInfo2)
    assert(r2.id == "scmUrl2")
    assert(r2.operation == "add")
  }

  "package.utils#uploadDocumentWrites" should "parse parse a UploadDocument" in {
    val app = new AppInfo("scmUrl", "serviceUrl", "created", "lastModified")
    val document = new UploadDocument("id", "add", app)
    val json = Json.toJson(document)
    implicit val uploadDocumentAppInfoFormat = uploadDocumentGenericFormat[UploadDocument[AppInfo]]
    Json.fromJson[UploadDocument[AppInfo]](Json.toJson(document)) match {
      case JsSuccess(result, _) =>
        assert(result === document)
      case e: JsError =>
        fail("Should not result in an error")

    }
  }
}