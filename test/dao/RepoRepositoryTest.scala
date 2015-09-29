package dao

import scala.concurrent.duration.DurationInt
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.inject.guice.GuiceApplicationBuilder
import test.util.FakeApplicationWithDB
import scala.concurrent.Await
import play.api.db.evolutions.Evolutions
import play.api.db.DBApi
import org.scalatestplus.play.OneAppPerSuite
import test.util.MockitoUtils
import play.api.Environment
import play.api.Configuration
import module.Development
import org.scalatest.BeforeAndAfterAll

class RepoRepositoryTest extends PlaySpec with MockitoUtils with MockitoSugar with FakeApplicationWithDB with BeforeAndAfterAll {

  val date1 = new DateTime
  val repo1 = createRepository(url = "url1", host = "host1", project = "project1", repository = "repository")
  val repo2 = createRepository(url = "url2", host = "host2", project = "project2", repository = "repository")

 
  override def afterAll {
    cleanupEvolutions
  }

  "RepoRepository#list" should {
    "be empty initially" in {
      val result = Await.result(repoRepository.all(), 5 seconds)
      assert(result.size == 0)
    }
  }

  "RepoRepository#save" should {
    "store data in the database" in {
      Await.result(repoRepository.save(List(repo1, repo2)), 5 seconds)
      val result = Await.result(repoRepository.all(), 15 seconds)
      assert(result.size == 2)
      assert(result.contains(repo1))
      assert(result.contains(repo2))
    }
  }

  def repoRepository = application.injector.instanceOf[RepoRepository]
}