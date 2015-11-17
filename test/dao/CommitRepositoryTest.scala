package dao

import org.joda.time.DateTime
import org.scalatest.BeforeAndAfter
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import test.util.ApplicationWithDB
import test.util.MockitoUtils
import org.scalatest._

/**
 * @author fbenjamin
 */
//@Ignore
class CommitRepositoryTest extends FlatSpec with Matchers with MockitoUtils with MockitoSugar with ApplicationWithDB with BeforeAndAfter {

  val prefix = "CommitRepositoryTest"
  val repo1 = createRepository(url = s"$prefix-url1", host = "host1", project = "project1", repository = "repository")
  val dateToday = new DateTime
  val dateYesterday = dateToday.minusDays(1)
  val dateBeforeYesterday = dateToday.minusDays(2)
  val dateOneWeekAgo = dateToday.minusWeeks(1)
  val dateOneMonthAgo = dateToday.minusMonths((1))

  val author1 = createAuthor("Author1name", "email", None)
  val tickets = Option(List(createTicket()))
  val commitToday = createCommit(s"$prefix-id1", "message", Option(List(s"$prefix-id2")), author1, tickets, Some(true), None, dateToday, repo1.url)
  val commitYesterday = createCommit(s"$prefix-id2", "message", Option(List(s"$prefix-id3")), author1, tickets, Some(true), None, dateYesterday, repo1.url)
  val commitBeforeYesterday = createCommit(s"$prefix-id3", "message", Option(List(s"$prefix-id4")), author1, tickets, Some(true), None, dateBeforeYesterday, repo1.url)
  val commitOneWeekAgo = createCommit(s"$prefix-id4", "message", None, author1, tickets, Some(true), None, dateOneWeekAgo, repo1.url)
  val commitOneMonthAgo = createCommit(s"$prefix-id5", "message", None, author1, None, Some(false), None, dateOneMonthAgo, repo1.url)

  override def beforeAll {
    Await.result(repoRepository.save(List(repo1)), 15.seconds)
    Await.result(commitRepository.save(List(commitToday, commitYesterday, commitBeforeYesterday, commitOneWeekAgo, commitOneMonthAgo)), 15.seconds)
  }
  "CommitRepository" must "save() & all() should save and return all commits in the table" in {
    val result = Await.result(commitRepository.all(), 15.seconds)
    assert(result.size === 5, "Two commits were saved.")
    assert(result.contains(commitToday), "commitToday should be saved")
    assert(result.contains(commitYesterday), "commitYesterday should be saved")
    assert(result.contains(commitBeforeYesterday), "commitBeforeYesterday should be saved")
    assert(result.contains(commitOneWeekAgo), "commitOneWeekAgo should be saved")
    assert(result.contains(commitOneMonthAgo), "commitOneMonthAgo should be saved")
  }
  it must "get commit by Id" in {
    val result = Await.result(commitRepository.byId(repo1.host, repo1.project, repo1.repository, commitToday.id), 15.seconds)
    assert(result.size === 1, "Should return single commit!!")
    assert(result.contains(commitToday))
  }
  it must "get commits between since 1-week-ago until yesterday" in {
    val result = Await.result(commitRepository.get(repo1.host, repo1.project, repo1.repository, Option(commitOneWeekAgo.id), Option(commitYesterday.id)), 15.seconds)
    assert(result.size === 3, "Only 3 Commits should be returned by the range query")
    assert(result.contains(commitYesterday), "commitYesterday should be in the result")
    assert(result.contains(commitBeforeYesterday), "commitBeforeYesterday should be in the result")
    assert(result.contains(commitOneWeekAgo), "commitOneWeekAgo should be in the result")
  }
  it must "get commits since yesterday" in {
    val result = Await.result(commitRepository.get(repo1.host, repo1.project, repo1.repository, Option(commitYesterday.id), None), 15.seconds)
    assert(result.size >= 2, "Only 2 Commits should be returned by the range query")
    assert(result.contains(commitToday), "commitToday should be in the result")
    assert(result.contains(commitYesterday), "commitYesterday should be in the result")
  }
  it must "get commits untill yesterday" in {
    val result = Await.result(commitRepository.get(repo1.host, repo1.project, repo1.repository, None, Option(commitYesterday.id)), 15.seconds)
    assert(result.size >= 4, "Only 4 Commits should be returned by the range query")
    assert(result.contains(commitYesterday), "commitYesterday should be in the result")
    assert(result.contains(commitBeforeYesterday), "commitBeforeYesterday should be in the result")
    assert(result.contains(commitOneWeekAgo), "commitOneWeekAgo should be in the result")
    assert(result.contains(commitOneMonthAgo), "commitOneMonthAgo should be in the result")
  }

  it must "get commits between since 1-week-ago until yesterday but pageNumber=2 and perPage=1" in {
    val result = Await.result(commitRepository.get(repo1.host, repo1.project, repo1.repository, Option(commitOneWeekAgo.id), Option(commitYesterday.id), pageNumber = Option(2), perPage = Option(1)), 15.seconds)
    assert(result.size === 1, "Only 1 Commit should be returned by the range query")
    assert(!result.contains(commitYesterday), "commitYesterday should be in the result")
    assert(result.contains(commitBeforeYesterday), "commitBeforeYesterday should be in the result")
    assert(!result.contains(commitOneWeekAgo), "commitOneWeekAgo should be in the result")
  }

  it must "get only invalid commits" in {
    val isValid = Some(false)
    val result = Await.result(commitRepository.get(repo1.host, repo1.project, repo1.repository, valid = isValid), 15.seconds)
    result foreach println
    assert(result.contains(commitOneMonthAgo), "commitOneMonthAgo should be in the result")
    assert(result(0).valid === isValid, "valid should be true")
  }
  it must "get only valid commits" in {
    val isValid = Some(true)
    val result = Await.result(commitRepository.get(repo1.host, repo1.project, repo1.repository, valid = isValid), 15.seconds)
    assert(result.size === 4, "Only 1 Invalid Commit should be returned")
    assert(!result.contains(commitOneMonthAgo), "commitOneMonthAgo should not be in the result")
    assert(result(0).valid === isValid, "valid should be true")
    assert(result(1).valid === isValid, "valid should be true")
    assert(result(2).valid === isValid, "valid should be true")
  }
  it must "get youngest commit" in {
    val result = Await.result(commitRepository.youngest(repo1.url), 15.seconds)
    assert(result.size === 1)
    assert(result.contains(commitToday), "commitToday should be in the result")
  }
  it must "get oldest commit" in {
    val result = Await.result(commitRepository.oldest(repo1.url), 15.seconds)
    assert(result.size === 1)
    assert(result.contains(commitOneMonthAgo), "commitOneMonthAgo should be in the result")
  }
  def repoRepository = application.injector.instanceOf[RepoRepository]
  def commitRepository = application.injector.instanceOf[CommitRepository]
}