package v1.test.util

object ParsingData {

  val repoSucceeders = List("", ".git", "/", "/browse", "/browse/", "/browse/dockerfiles/fashion-advice/master", "/browse/asdf/lest")
  val repos = List("___", "-", "Test_Repo", "repo", "...", "_-", "1_1_2_3_", "ZKON_-A.ZKON_-A.", "_Guess-_11_-D", "Proje123", "Test-repo")
  val repoAntecedents = List("/", "/repos/")
  val projects = List("-", "Test_Now", "asdf", "...", "_-", "1_1_2_3_", "ZKON_-A.ZKON_-A.", "_Guess-_11_-D", "Proje123", "Test-project")
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
      antecedent <- projectAntecedents
    } yield (protocolUserHost + antecedent)

    val protocolUserHostAntecedentsProjects = for {
      protocolUserHostAntecedent <- protocolUserHostAntecedents
      project <- projects
    } yield (protocolUserHostAntecedent + project)

    val protocolUserHostAntecedentsProjectAntecedents = for {
      protocolUserHostAntecedentsProject <- protocolUserHostAntecedentsProjects
      antecedent <- repoAntecedents
    } yield (protocolUserHostAntecedentsProject + antecedent)

    val protocolUserHostAntecedentsProjectAntecedentRepos = for {
      protocolUserHostAntecedentsProjectAntecedent <- protocolUserHostAntecedentsProjectAntecedents
      repo <- repos
    } yield (protocolUserHostAntecedentsProjectAntecedent + repo)

    val protocolUserHostAntecedentsProjectAntecedentReposRepoSucceeders = for {
      protocolUserHostAntecedentsProjectAntecedentRepo <- protocolUserHostAntecedentsProjectAntecedentRepos
      repoSucceeder <- repoSucceeders
    } yield (protocolUserHostAntecedentsProjectAntecedentRepo + repoSucceeder)
  }

}

object FakeResponseData {

  val multiStashCommit = """
       "values": [
        {
            "id": "d7d99a9ee6aa9c3d0960f1591fddf78f65171dd9",
            "displayId": "d7d99a9ee6a",
            "author": {
                "name": "Fernando Benjamin",
                "emailAddress": "benibadboy@hotmail.com"
            },
            "authorTimestamp": 1430753260000,
            "message": "Remove comments",
            "parents": [
                {
                    "id": "9405c626889dbe91694c7dab33eb091a9483317e",
                    "displayId": "9405c626889"
                }
            ]
        },
        {
            "id": "9405c626889dbe91694c7dab33eb091a9483317e",
            "displayId": "9405c626889",
            "author": {
                "name": "Fernando Benjamin",
                "emailAddress": "benibadboy@hotmail.com"
            },
            "authorTimestamp": 1430744119000,
            "message": "Testing",
            "parents": [
                {
                    "id": "1a4ed65260f854d35c1ab01a6113964f8fc24414",
                    "displayId": "1a4ed65260f"
                }
            ]
        }
        ]
    
    """
  val multiGithubCommit = """
    [
     {
        "sha": "50cea1156ca558eb6c67e78ca7e5dabc570ea99a",
        "commit": {
            "author": {
                "name": "Fernando Benjamin",
                "email": "kanuku@users.noreply.github.com",
                "date": "2015-05-11T08:33:24Z"
            },
            "committer": {
                "name": "Fernando Benjamin",
                "email": "kanuku@users.noreply.github.com",
                "date": "2015-05-11T08:33:24Z"
            },
            "message": "Merge pull request #8 from zalando-bus/feature-swagger-first\n\nApi Specification in Swagger",
            "tree": {
                "sha": "6336b59c25540296d4d6d718fbee2480403e756f",
                "url": "https://api.github.com/repos/zalando-bus/kontrolletti/git/trees/6336b59c25540296d4d6d718fbee2480403e756f"
            },
            "url": "https://api.github.com/repos/zalando-bus/kontrolletti/git/commits/50cea1156ca558eb6c67e78ca7e5dabc570ea99a",
            "comment_count": 0
        },
        "url": "https://api.github.com/repos/zalando-bus/kontrolletti/commits/50cea1156ca558eb6c67e78ca7e5dabc570ea99a",
        "html_url": "https://github.com/zalando-bus/kontrolletti/commit/50cea1156ca558eb6c67e78ca7e5dabc570ea99a",
        "comments_url": "https://api.github.com/repos/zalando-bus/kontrolletti/commits/50cea1156ca558eb6c67e78ca7e5dabc570ea99a/comments",
        "author": {
            "login": "kanuku",
            "id": 4045639,
            "avatar_url": "https://avatars.githubusercontent.com/u/4045639?v=3",
            "gravatar_id": "",
            "url": "https://api.github.com/users/kanuku",
            "html_url": "https://github.com/kanuku",
            "followers_url": "https://api.github.com/users/kanuku/followers",
            "following_url": "https://api.github.com/users/kanuku/following{/other_user}",
            "gists_url": "https://api.github.com/users/kanuku/gists{/gist_id}",
            "starred_url": "https://api.github.com/users/kanuku/starred{/owner}{/repo}",
            "subscriptions_url": "https://api.github.com/users/kanuku/subscriptions",
            "organizations_url": "https://api.github.com/users/kanuku/orgs",
            "repos_url": "https://api.github.com/users/kanuku/repos",
            "events_url": "https://api.github.com/users/kanuku/events{/privacy}",
            "received_events_url": "https://api.github.com/users/kanuku/received_events",
            "type": "User",
            "site_admin": false
        },
        "committer": {
            "login": "kanuku",
            "id": 4045639,
            "avatar_url": "https://avatars.githubusercontent.com/u/4045639?v=3",
            "gravatar_id": "",
            "url": "https://api.github.com/users/kanuku",
            "html_url": "https://github.com/kanuku",
            "followers_url": "https://api.github.com/users/kanuku/followers",
            "following_url": "https://api.github.com/users/kanuku/following{/other_user}",
            "gists_url": "https://api.github.com/users/kanuku/gists{/gist_id}",
            "starred_url": "https://api.github.com/users/kanuku/starred{/owner}{/repo}",
            "subscriptions_url": "https://api.github.com/users/kanuku/subscriptions",
            "organizations_url": "https://api.github.com/users/kanuku/orgs",
            "repos_url": "https://api.github.com/users/kanuku/repos",
            "events_url": "https://api.github.com/users/kanuku/events{/privacy}",
            "received_events_url": "https://api.github.com/users/kanuku/received_events",
            "type": "User",
            "site_admin": false
        },
        "parents": [
            {
                "sha": "88c31c976507b32574bb9c76311da1cfc4832d1d",
                "url": "https://api.github.com/repos/zalando-bus/kontrolletti/commits/88c31c976507b32574bb9c76311da1cfc4832d1d",
                "html_url": "https://github.com/zalando-bus/kontrolletti/commit/88c31c976507b32574bb9c76311da1cfc4832d1d"
            },
            {
                "sha": "2ead1df4182c33bbca16768e4200a09ce3b6e68d",
                "url": "https://api.github.com/repos/zalando-bus/kontrolletti/commits/2ead1df4182c33bbca16768e4200a09ce3b6e68d",
                "html_url": "https://github.com/zalando-bus/kontrolletti/commit/2ead1df4182c33bbca16768e4200a09ce3b6e68d"
            }
        ]
    },
    {
        "sha": "2ead1df4182c33bbca16768e4200a09ce3b6e68d",
        "commit": {
            "author": {
                "name": "Fernando Benjamin",
                "email": "benibadboy@hotmail.com",
                "date": "2015-05-11T08:07:20Z"
            },
            "committer": {
                "name": "Fernando Benjamin",
                "email": "benibadboy@hotmail.com",
                "date": "2015-05-11T08:07:20Z"
            },
            "message": "Swagger specification is ready.",
            "tree": {
                "sha": "6336b59c25540296d4d6d718fbee2480403e756f",
                "url": "https://api.github.com/repos/zalando-bus/kontrolletti/git/trees/6336b59c25540296d4d6d718fbee2480403e756f"
            },
            "url": "https://api.github.com/repos/zalando-bus/kontrolletti/git/commits/2ead1df4182c33bbca16768e4200a09ce3b6e68d",
            "comment_count": 0
        },
        "url": "https://api.github.com/repos/zalando-bus/kontrolletti/commits/2ead1df4182c33bbca16768e4200a09ce3b6e68d",
        "html_url": "https://github.com/zalando-bus/kontrolletti/commit/2ead1df4182c33bbca16768e4200a09ce3b6e68d",
        "comments_url": "https://api.github.com/repos/zalando-bus/kontrolletti/commits/2ead1df4182c33bbca16768e4200a09ce3b6e68d/comments",
        "author": {
            "login": "kanuku",
            "id": 4045639,
            "avatar_url": "https://avatars.githubusercontent.com/u/4045639?v=3",
            "gravatar_id": "",
            "url": "https://api.github.com/users/kanuku",
            "html_url": "https://github.com/kanuku",
            "followers_url": "https://api.github.com/users/kanuku/followers",
            "following_url": "https://api.github.com/users/kanuku/following{/other_user}",
            "gists_url": "https://api.github.com/users/kanuku/gists{/gist_id}",
            "starred_url": "https://api.github.com/users/kanuku/starred{/owner}{/repo}",
            "subscriptions_url": "https://api.github.com/users/kanuku/subscriptions",
            "organizations_url": "https://api.github.com/users/kanuku/orgs",
            "repos_url": "https://api.github.com/users/kanuku/repos",
            "events_url": "https://api.github.com/users/kanuku/events{/privacy}",
            "received_events_url": "https://api.github.com/users/kanuku/received_events",
            "type": "User",
            "site_admin": false
        },
        "committer": {
            "login": "kanuku",
            "id": 4045639,
            "avatar_url": "https://avatars.githubusercontent.com/u/4045639?v=3",
            "gravatar_id": "",
            "url": "https://api.github.com/users/kanuku",
            "html_url": "https://github.com/kanuku",
            "followers_url": "https://api.github.com/users/kanuku/followers",
            "following_url": "https://api.github.com/users/kanuku/following{/other_user}",
            "gists_url": "https://api.github.com/users/kanuku/gists{/gist_id}",
            "starred_url": "https://api.github.com/users/kanuku/starred{/owner}{/repo}",
            "subscriptions_url": "https://api.github.com/users/kanuku/subscriptions",
            "organizations_url": "https://api.github.com/users/kanuku/orgs",
            "repos_url": "https://api.github.com/users/kanuku/repos",
            "events_url": "https://api.github.com/users/kanuku/events{/privacy}",
            "received_events_url": "https://api.github.com/users/kanuku/received_events",
            "type": "User",
            "site_admin": false
        },
        "parents": [
            {
                "sha": "ca0003e2beba64c96150f03a3cd1d84c58c6a469",
                "url": "https://api.github.com/repos/zalando-bus/kontrolletti/commits/ca0003e2beba64c96150f03a3cd1d84c58c6a469",
                "html_url": "https://github.com/zalando-bus/kontrolletti/commit/ca0003e2beba64c96150f03a3cd1d84c58c6a469"
            }
        ]
    }
    ]    
    """

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