

package v1.service

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import play.api.test.Helpers._
import v1.client.Github

@RunWith(classOf[JUnitRunner])
class SearchServiceTest extends FunSuite {
  val githubClient = new Github
  val searchService: Search = new SearchImpl(githubClient)

}