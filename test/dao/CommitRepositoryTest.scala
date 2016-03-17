package dao

import model.Commit
import model.Repository
import model.Ticket
import org.joda.time.DateTime
import org.scalacheck.Arbitrary
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.Checkers
import org.scalatest.{BeforeAndAfter, _}
import org.scalatest.mock.MockitoSugar
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import test.util.{generator, ApplicationWithDB, MockitoUtils}
import play.api.db.DBApi

import scala.util.Random
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext

/**
 * @author fbenjamin
 */
//@Ignore
class CommitRepositoryTest extends FunSuite with Matchers with MockitoUtils with MockitoSugar with ApplicationWithDB with BeforeAndAfter with ScalaFutures with Checkers {

  def repoRepository = application.injector.instanceOf[RepoRepository]
  def commitRepository = application.injector.instanceOf[CommitRepository]
  val dbConfig = application.injector.instanceOf[DatabaseConfigProvider].get[JdbcProfile]
  implicit def ec = application.injector.instanceOf[ExecutionContext]
  implicit val arbRepoCommits = Arbitrary(generator.genRepoCommits)
  import dbConfig.driver.api._

  val deleteCommits = sqlu"DELETE FROM kont_data.commits"
  val deleteRepos = sqlu"DELETE FROM kont_data.repositories"
  val cleanupContent = DBIO.seq(deleteCommits, deleteRepos)

  def savingAll(repo: Repository, commits: List[Commit])(implicit ec: ExecutionContext) = for {
    _ <- repoRepository.save(List(repo))
    _ <- commitRepository.save(commits)
  } yield ()

  test("CommitRepository#all should return all results") {
    check { rcs: (Repository, List[Commit]) =>
      val (repo, commits) = rcs
      val saving = savingAll(repo, commits)

      Await.ready(saving, 10.seconds)
      val result = Await.result(commitRepository.all, 10.seconds)
      Await.ready(dbConfig.db.run(cleanupContent), 10.seconds)
      commits.size == result.size
    }
  }

  test("CommitRepository#byId should return commit with given id") {
    check {
      rcs: (Repository, List[Commit]) =>
      val (repo, commits) = rcs
      val repoParam = RepoParameters(repo.host, repo.project, repo.repository)
      val saving = savingAll(repo, commits)

      Await.ready(saving, 10.seconds)
      val compareOpt = for {
        targetCommit <- Random.shuffle(commits).headOption
        resultCommit <- Await.result(commitRepository.byId(repoParam, targetCommit.id), 10.seconds)
      } yield targetCommit == resultCommit
      Await.ready(dbConfig.db.run(cleanupContent), 10.seconds)
      compareOpt getOrElse true
    }
  }

  test("CommitRepository#get should return commits between from and to date") {
    check {
      rcs: (Repository, List[Commit]) =>
      val (repo, commits) = rcs
      val repoParam = RepoParameters(repo.host, repo.project, repo.repository)
      val saving = savingAll(repo, commits)

      Await.ready(saving, 10.seconds)
      val resultOpt = for {
        sinceCommit <- Random.shuffle(commits).headOption
        untilCommit <- Random.shuffle(commits.collect { case c if c.date.isAfter(sinceCommit.date) => c}).headOption
        sinceDate = sinceCommit.date.withTimeAtStartOfDay
        untilDate = untilCommit.date.withTimeAtStartOfDay
        PagedResult(results, _) = Await.result(commitRepository.get(repoParam, FilterParameters(sinceDate = Some(sinceDate), untilDate = Some(untilDate)), PageParameters(perPage = Some(commits.size))), 10.seconds)
      } yield results.forall { commit =>
        (commit.date.isEqual(sinceDate) || commit.date.isAfter(sinceDate)) &&
        (commit.date.isEqual(untilDate) || commit.date.isBefore(untilDate))
      }
      Await.ready(dbConfig.db.run(cleanupContent), 10.seconds)
      resultOpt getOrElse true
    }
  }

  test("CommitRepository#get should return commits after from date") {
    check {
      rcs: (Repository, List[Commit]) =>
      val (repo, commits) = rcs
      val repoParam = RepoParameters(repo.host, repo.project, repo.repository)
      val saving = savingAll(repo, commits)

      Await.ready(saving, 10.seconds)
      val resultOpt = for {
        sinceCommit <- Random.shuffle(commits).headOption
        sinceDate = sinceCommit.date.withTimeAtStartOfDay
        PagedResult(results, _) = Await.result(commitRepository.get(repoParam, FilterParameters(sinceDate = Some(sinceDate), untilDate = None), PageParameters(perPage = Some(commits.size))), 10.seconds)
      } yield results.forall { commit =>
        (commit.date.isEqual(sinceDate) || commit.date.isAfter(sinceDate))
      }
      Await.ready(dbConfig.db.run(cleanupContent), 10.seconds)
      resultOpt getOrElse true
    }
  }

  test("CommitRepository#get should return commits before to date") {
    check {
      rcs: (Repository, List[Commit]) =>
      val (repo, commits) = rcs
      val repoParam = RepoParameters(repo.host, repo.project, repo.repository)
      val saving = savingAll(repo, commits)

      Await.ready(saving, 10.seconds)
      val resultOpt = for {
        untilCommit <- Random.shuffle(commits).headOption
        untilDate = untilCommit.date.withTimeAtStartOfDay
        PagedResult(results, _) = Await.result(commitRepository.get(repoParam, FilterParameters(untilDate = Some(untilDate), sinceDate = None), PageParameters(perPage = Some(commits.size))), 10.seconds)
      } yield results.forall { commit =>
        (commit.date.isEqual(untilDate) || commit.date.isBefore(untilDate))
      }
      Await.ready(dbConfig.db.run(cleanupContent), 10.seconds)
      resultOpt getOrElse true
    }
  }

  test("CommitRepository#get valid should get only valid commit") {
    check {
      rcs: (Repository, List[Commit]) =>
      val (repo, commits) = rcs
      val repoParam = RepoParameters(repo.host, repo.project, repo.repository)
      val saving = savingAll(repo, commits)

      Await.ready(saving, 10.seconds)
      val PagedResult(results, _) = Await.result(commitRepository.get(repoParam, FilterParameters(valid = Some(true)), PageParameters(perPage = Some(commits.size))), 10.seconds)
      Await.ready(dbConfig.db.run(cleanupContent), 10.seconds)
      results.size == commits.filter(_.tickets.toList.flatten.nonEmpty).size
    }
  }

  test("CommitRepository#get invalid should get only invalid commit") {
    check {
      rcs: (Repository, List[Commit]) =>
      val (repo, commits) = rcs
      val repoParam = RepoParameters(repo.host, repo.project, repo.repository)
      val saving = savingAll(repo, commits)

      Await.ready(saving, 10.seconds)
      val PagedResult(results, _) = Await.result(commitRepository.get(repoParam, FilterParameters(valid = Some(false)), PageParameters(perPage = Some(commits.size))), 10.seconds)
      Await.ready(dbConfig.db.run(cleanupContent), 10.seconds)
      results.size == commits.filter(_.tickets.toList.flatten.isEmpty).size
    }
  }

  test("CommitRepository#youngest should get youngest commit") {
    check {
      rcs: (Repository, List[Commit]) =>
      val (repo, commits) = rcs
      val saving = savingAll(repo, commits)

      Await.ready(saving, 10.seconds)
      val resultOpt = Await.result(commitRepository.youngest(repo.url), 10.seconds) map { youngest =>
        commits.filter(c => c.date.isAfter(youngest.date)).isEmpty
      }
      Await.ready(dbConfig.db.run(cleanupContent), 10.seconds)
      resultOpt getOrElse true
    }
  }

  test("CommitRepository#oldest should get oldest commit") {
    check {
      rcs: (Repository, List[Commit]) =>
      val (repo, commits) = rcs
      val saving = savingAll(repo, commits)

      Await.ready(saving, 10.seconds)
      val resultOpt = Await.result(commitRepository.oldest(repo.url), 10.seconds) map { oldest =>
        commits.filter(c => c.date.isBefore(oldest.date)).isEmpty
      }
      Await.ready(dbConfig.db.run(cleanupContent), 10.seconds)
      resultOpt getOrElse true
    }
  }


  // TODO: valid & invalid commits

  test("CommitRepository#tickets should get all tickets") {
    check {
      rcs: (Repository, List[Commit]) =>
      val (repo, commits) = rcs
      val tickets: List[Ticket] = for {
        commit <- commits
        t      <- commit.tickets.toList.flatten
      } yield t
      val repoParam = RepoParameters(repo.host, repo.project, repo.repository)
      val saving = savingAll(repo, commits)

      Await.ready(saving, 10.seconds)
      val PagedResult(results, _) = Await.result(commitRepository.tickets(repoParam, FilterParameters(), PageParameters(perPage = Some(tickets.size))), 10.seconds)
      Await.ready(dbConfig.db.run(cleanupContent), 10.seconds)
      results.size == tickets.size
    }
  }

  // TODO: test pagination for tickets, get
}
