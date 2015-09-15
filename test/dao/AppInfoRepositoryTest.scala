package dao

import scala.concurrent.duration.DurationInt
import org.scalatest.FlatSpec
import org.scalatestplus.play.PlaySpec
import model.AppInfo
import play.api.inject.guice.GuiceApplicationBuilder
import test.util.KontrollettiFakeApplication
import test.util.MockitoUtils
import scala.concurrent.Await
import org.scalatest.mock.MockitoSugar
import org.scalatest.BeforeAndAfter

class AppInfoRepositoryTest extends PlaySpec with MockitoUtils with MockitoSugar with KontrollettiFakeApplication with BeforeAndAfter {
  withTestDatabaseConfigured {
    val app1 = createAppInfo("scmUrl1", None, None, None)
    val app2 = createAppInfo("scmUrl2", None, None, None)

    before(appInfoRepository.initializeDatabase)

    "AppInfoRepository#list" should {
      "be empty initially" in {
        val result = Await.result(appInfoRepository.list(), 5 seconds)
        assert(result.size == 0)
      }
    }

    "AppInfoRepository#save" should {
      "store data in the database" in {
        val appInfoRepo = appInfoRepository
        Await.result(appInfoRepo.saveApps(List(app1, app2)), 5 seconds)
        val result = Await.result(appInfoRepository.list(), 15 seconds)
        assert(result.size == 2)
        assert(result.contains(app1))
        assert(result.contains(app2))
      }
    }
  }

  def appInfoRepository = application.injector().instanceOf[AppInfoRepository]

}