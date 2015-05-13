package v1.test.util

object ParsingData {

  val projects = List("-", "Test_Now", "asdf", "...", "_-", "1_1_2_3_", "ZKON_-A.ZKON_-A.", "_Guess-_11_-D", "Proje123", "Test-project")
  //Github has none, but stash does it REST-STyle /projects/
  val projectAntecedents = List("/", "/projects/")
  val ports = List("", ":8080", ":80")
  val hostnames = List("stash", "stash-server", "stash.net", "stash-zalando", "live-stash-zalando", "stash.cd.zalando")
  val users = List("", "git@", "fbenjamin@")
  val protocols = List("", "http://", "http://", "https://", "ssh://")

  def fixture = new {
    val hosts =
      for {
        host <- hostnames
        port <- ports
      } yield (host + port)

    val protocolUsers = for {
      protocol <- protocols
      user <- users
    } yield (protocol + user)

    val protocolUserHosts = for {
      protocolUser <- protocolUsers
      host <- hosts
    } yield (protocolUser + host)
    
    val protocolUserHostAntecedents = for {
      protocolUserHost <- protocolUserHosts
     antecedent  <- projectAntecedents 
    } yield (protocolUserHost + antecedent)
  }

}

trait Data {

  val ghUsers = """
  {
  "login" : "kanuku",
  "id" : 4045639,
  "avatar_url" : "https://avatars.githubusercontent.com/u/4045639?v=3",
  "gravatar_id" : "",
  "url" : "https://api.github.com/users/kanuku",
  "html_url" : "https://github.com/kanuku",
  "followers_url" : "https://api.github.com/users/kanuku/followers",
  "following_url" : "https://api.github.com/users/kanuku/following{/other_user}",
  "gists_url" : "https://api.github.com/users/kanuku/gists{/gist_id}",
  "starred_url" : "https://api.github.com/users/kanuku/starred{/owner}{/repo}",
  "subscriptions_url" : "https://api.github.com/users/kanuku/subscriptions",
  "organizations_url" : "https://api.github.com/users/kanuku/orgs",
  "repos_url" : "https://api.github.com/users/kanuku/repos",
  "events_url" : "https://api.github.com/users/kanuku/events{/privacy}",
  "received_events_url" : "https://api.github.com/users/kanuku/received_events",
  "type" : "User",
  "site_admin" : false,
  "contributions" : 43
}, {
  "login" : "jmcs",
  "id" : 3719966,
  "avatar_url" : "https://avatars.githubusercontent.com/u/3719966?v=3",
  "gravatar_id" : "",
  "url" : "https://api.github.com/users/jmcs",
  "html_url" : "https://github.com/jmcs",
  "followers_url" : "https://api.github.com/users/jmcs/followers",
  "following_url" : "https://api.github.com/users/jmcs/following{/other_user}",
  "gists_url" : "https://api.github.com/users/jmcs/gists{/gist_id}",
  "starred_url" : "https://api.github.com/users/jmcs/starred{/owner}{/repo}",
  "subscriptions_url" : "https://api.github.com/users/jmcs/subscriptions",
  "organizations_url" : "https://api.github.com/users/jmcs/orgs",
  "repos_url" : "https://api.github.com/users/jmcs/repos",
  "events_url" : "https://api.github.com/users/jmcs/events{/privacy}",
  "received_events_url" : "https://api.github.com/users/jmcs/received_events",
  "type" : "User",
  "site_admin" : false,
  "contributions" : 2
} ]
"""

}