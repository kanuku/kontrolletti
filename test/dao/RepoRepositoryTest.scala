package dao

import scala.concurrent.duration.DurationInt
import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.inject.guice.GuiceApplicationBuilder
import test.util.ApplicationWithDB
import scala.concurrent.Await
import play.api.db.evolutions.Evolutions
import play.api.db.DBApi
import org.scalatestplus.play.OneAppPerSuite
import test.util.MockitoUtils
import play.api.Environment
import play.api.Configuration
import module.Development
import org.scalatest.BeforeAndAfterAll

class RepoRepositoryTest extends PlaySpec with MockitoUtils with MockitoSugar with ApplicationWithDB {

  val date1 = new DateTime
  val enabledRepo1 = createRepository(url = "url1", host = "host1", project = "RepoRepositoryTest-project1", repository = "repository")
  val disabledRepo2 = createRepository(url = "url2", host = "host2", project = "RepoRepositoryTest-project2", repository = "repository",enabled=false)


  "RepoRepository" should {
	  Await.result(repoRepository.save(List(enabledRepo1, disabledRepo2)), 5 seconds)
    "store data in the database" in {
      val result = Await.result(repoRepository.all(), 15 seconds)
      assert(result.size >= 2)
      assert(result.contains(enabledRepo1))
      assert(result.contains(disabledRepo2))
    }
	  
	  "return enabled repositories" in {
		  val result = Await.result(repoRepository.enabled(), 15 seconds)
				  assert(result.size > 0)
				  assert(result.contains(enabledRepo1), "Enabled repository should be in the result")
				  assert(!result.contains(disabledRepo2), "Disabled repository should not be in the result")
	    
	  }
	  "return single result by parameters" in {
		  val Some(result) = Await.result(repoRepository.byParameters(enabledRepo1.host, enabledRepo1.project, enabledRepo1.repository), 15 seconds)
		  assert(result === enabledRepo1,"Enabled repository should be the only returned result")
	  }
  }
  

  def repoRepository = application.injector.instanceOf[RepoRepository]
}