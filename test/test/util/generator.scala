package test.util

import org.joda.time.DateTime
import org.scalacheck.Gen
import model.{Author, Commit, Repository, Ticket}

object generator {

  object ticket {
    lazy val genJiraTicket = for {
      prefix <- Gen.nonEmptyListOf(Gen.alphaChar).map(_.mkString)
      suffix <- Gen.nonEmptyListOf(Gen.numChar).map(_.mkString)
    } yield prefix.toUpperCase + "-" + suffix

    lazy val genTechJiraTicket = for {
      prefix <- Gen.oneOf("techjira", "jira")
      ticket <- genJiraTicket
    } yield prefix + ":" + ticket

    lazy val genHashTagTicket =
      Gen.nonEmptyListOf(Gen.numChar).map("#" + _.mkString)

    lazy val genHashTagWithScmRefSuffixTicket =
      for {
        ticket <- genHashTagTicket
        suffix <- Gen.oneOf("gh", "ghe")
      } yield ticket + s" ($suffix)"

    lazy val genScmRefPrefixTicket =
      for {
        prefix <- Gen.oneOf("GH", "GHE")
        ticket <- Gen.nonEmptyListOf(Gen.alphaChar).map(_.mkString)
      } yield prefix + "-" + ticket

    // TODO: add generator for url ticket with & without scheme

    lazy val genTicketRef =
      Gen.oneOf(
        genJiraTicket,
        genTechJiraTicket,
        genHashTagTicket,
        genHashTagWithScmRefSuffixTicket,
        genScmRefPrefixTicket
      )

    // TODO: add proper links to ticket
    lazy val genTicket = for {
      name <- genTicketRef
      href <- Gen.alphaStr
    } yield model.Ticket(name, href, None)
  }

  object author {

    // TODO: make it proper
    lazy val genAuthor = for {
      name <- Gen.alphaStr
      email <- Gen.alphaStr
    } yield model.Author(name, email, None)
  }

  object repo {

    // TODO: make it proper
    lazy val genRepo = for {
      url <- Gen.listOfN(15, Gen.alphaLowerChar)
      host <- Gen.alphaStr
      project <- Gen.alphaStr
      repository <- Gen.alphaStr
      enabled <- Gen.oneOf(true, false)
    } yield model.Repository(
      url = url.mkString,
      host = host,
      project = project,
      repository = repository,
      enabled = enabled,
      lastSync = None,
      lastFailed = None,
      links = None
    )

    lazy val genRepos = Gen.listOf(genRepo).map {
      _.foldLeft(List.empty[Repository]) {
        case (acc, curr) =>
          if (acc.exists((r: Repository) => r.url == curr.url)) acc
          else curr :: acc
      }
    }
  }

  object commit {
    import ticket.{genTicketRef, genTicket}
    import author.genAuthor

    lazy val genCommitMsgWithTicket = for {
      prefix <- Gen.alphaStr
      suffix <- Gen.alphaStr
      ticket <- genTicketRef
    } yield prefix + " " + ticket + " " + suffix

    lazy val genCommitMsg = Gen.oneOf(Gen.alphaStr, genCommitMsgWithTicket)

    // TODO: add proper ticket, which depends on repo's info
    def genCommit(repo: Repository) = for {
      id <- Gen.nonEmptyListOf(Gen.alphaNumChar)
      msg <- genCommitMsg
      author <- genAuthor
      ticketsOpt <- Gen.option(Gen.listOf(genTicket))
      valid <- Gen.option(Gen.oneOf(true, false))
      dt <- genDateTime
    } yield model.Commit(
      id = id.mkString,
      message = msg,
      parentIds = None,
      author = author,
      tickets = ticketsOpt,
      valid = valid,
      links = None,
      repoUrl = repo.url,
      date = dt
    )

    def genCommits(repo: Repository) =
      Gen.listOf(genCommit(repo)).map(_.foldLeft(List.empty[Commit]) {
        case (acc, curr) =>
          if (acc.exists(c => c.id == curr.id )) acc
          else curr :: acc
      })
  }

  lazy val genDateTime = Gen.choose(0L, DateTime.now.getMillis).map(l => new DateTime(l))
  lazy val genRepoCommits: Gen[(Repository, List[Commit])] = for {
    r <- repo.genRepo
    cs <- commit.genCommits(r)
  } yield (r, cs)
}
