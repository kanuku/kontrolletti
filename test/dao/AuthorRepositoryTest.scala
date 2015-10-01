package dao

import scala.concurrent.duration.DurationInt
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.inject.guice.GuiceApplicationBuilder
import test.util.ApplicationWithDB
import test.util.MockitoUtils
import scala.concurrent.Await
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Ignore

@Ignore
class AuthorRepositoryTest extends PlaySpec with MockitoUtils with MockitoSugar with ApplicationWithDB with BeforeAndAfterAll {
  val link1 = createLink("href", "method", "rel", "relType")
  val link2 = createLink("href2", "method", "rel", "relType")
  val links = List(link1, link2)
  val author1 = createAuthor("name", "email1", Option(links))
  val author2 = createAuthor("name", "email2", Option(links))
  val author3 = createAuthor("name", "email2", Option(links))

  override def beforeAll {
    cleanupEvolutions

  }

  override def afterAll {
    cleanupEvolutions
  }

  "AuthorRepository#list" should {
    "be empty initially" in {
      val result = Await.result(authorRepo.list(), 15 seconds)
      assert(result.size == 0)
    }
  }

  "AuthorRepository#save" should {
    "store data in the database" in {
      Await.result(authorRepo.save(List(author1, author2)), 15 seconds)
      val result = Await.result(authorRepo.list(), 15 seconds)
      assert(result.size == 2, "The number of inserted authors does not match!!")
      assert(result.contains(author1), "author1 should be returned")
      assert(result.contains(author2), "author1 should be returned")
      assert(result(0).links === links, "links should be exact the same")
    }
  }
  "AuthorRepository#list" should {
    "return all stored Authors" in {

    }

  }

  def authorRepo = application.injector.instanceOf[AuthorRepository]

}