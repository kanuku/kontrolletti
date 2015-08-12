package test.util

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
       {"values": [
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
                },
                {
                    "id": "ab33eb091a9483317e9405c626889dbe91694c7d",
                    "displayId": "268899405c6"
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
        ]}
    
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
   [{
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
    }]
"""
val ghRepo = """
    {
      "id" : 33127716,
      "name" : "kontrolletti",
      "full_name" : "zalando/kontrolletti",
      "owner" : {
        "login" : "zalando",
        "id" : 1564818,
        "avatar_url" : "https://avatars.githubusercontent.com/u/1564818?v=3",
        "gravatar_id" : "",
        "url" : "https://api.github.com/users/zalando",
        "html_url" : "https://github.com/zalando",
        "followers_url" : "https://api.github.com/users/zalando/followers",
        "following_url" : "https://api.github.com/users/zalando/following{/other_user}",
        "gists_url" : "https://api.github.com/users/zalando/gists{/gist_id}",
        "starred_url" : "https://api.github.com/users/zalando/starred{/owner}{/repo}",
        "subscriptions_url" : "https://api.github.com/users/zalando/subscriptions",
        "organizations_url" : "https://api.github.com/users/zalando/orgs",
        "repos_url" : "https://api.github.com/users/zalando/repos",
        "events_url" : "https://api.github.com/users/zalando/events{/privacy}",
        "received_events_url" : "https://api.github.com/users/zalando/received_events",
        "type" : "Organization",
        "site_admin" : false
      },
      "private" : false,
      "html_url" : "https://github.com/zalando/kontrolletti",
      "description" : "The REST API that aggregates and unifies information from different Source Control Management for historical and auditing purposes.",
      "fork" : false,
      "url" : "https://api.github.com/repos/zalando/kontrolletti",
      "forks_url" : "https://api.github.com/repos/zalando/kontrolletti/forks",
      "keys_url" : "https://api.github.com/repos/zalando/kontrolletti/keys{/key_id}",
      "collaborators_url" : "https://api.github.com/repos/zalando/kontrolletti/collaborators{/collaborator}",
      "teams_url" : "https://api.github.com/repos/zalando/kontrolletti/teams",
      "hooks_url" : "https://api.github.com/repos/zalando/kontrolletti/hooks",
      "issue_events_url" : "https://api.github.com/repos/zalando/kontrolletti/issues/events{/number}",
      "events_url" : "https://api.github.com/repos/zalando/kontrolletti/events",
      "assignees_url" : "https://api.github.com/repos/zalando/kontrolletti/assignees{/user}",
      "branches_url" : "https://api.github.com/repos/zalando/kontrolletti/branches{/branch}",
      "tags_url" : "https://api.github.com/repos/zalando/kontrolletti/tags",
      "blobs_url" : "https://api.github.com/repos/zalando/kontrolletti/git/blobs{/sha}",
      "git_tags_url" : "https://api.github.com/repos/zalando/kontrolletti/git/tags{/sha}",
      "git_refs_url" : "https://api.github.com/repos/zalando/kontrolletti/git/refs{/sha}",
      "trees_url" : "https://api.github.com/repos/zalando/kontrolletti/git/trees{/sha}",
      "statuses_url" : "https://api.github.com/repos/zalando/kontrolletti/statuses/{sha}",
      "languages_url" : "https://api.github.com/repos/zalando/kontrolletti/languages",
      "stargazers_url" : "https://api.github.com/repos/zalando/kontrolletti/stargazers",
      "contributors_url" : "https://api.github.com/repos/zalando/kontrolletti/contributors",
      "subscribers_url" : "https://api.github.com/repos/zalando/kontrolletti/subscribers",
      "subscription_url" : "https://api.github.com/repos/zalando/kontrolletti/subscription",
      "commits_url" : "https://api.github.com/repos/zalando/kontrolletti/commits{/sha}",
      "git_commits_url" : "https://api.github.com/repos/zalando/kontrolletti/git/commits{/sha}",
      "comments_url" : "https://api.github.com/repos/zalando/kontrolletti/comments{/number}",
      "issue_comment_url" : "https://api.github.com/repos/zalando/kontrolletti/issues/comments{/number}",
      "contents_url" : "https://api.github.com/repos/zalando/kontrolletti/contents/{path}",
      "compare_url" : "https://api.github.com/repos/zalando/kontrolletti/compare/{base}...{head}",
      "merges_url" : "https://api.github.com/repos/zalando/kontrolletti/merges",
      "archive_url" : "https://api.github.com/repos/zalando/kontrolletti/{archive_format}{/ref}",
      "downloads_url" : "https://api.github.com/repos/zalando/kontrolletti/downloads",
      "issues_url" : "https://api.github.com/repos/zalando/kontrolletti/issues{/number}",
      "pulls_url" : "https://api.github.com/repos/zalando/kontrolletti/pulls{/number}",
      "milestones_url" : "https://api.github.com/repos/zalando/kontrolletti/milestones{/number}",
      "notifications_url" : "https://api.github.com/repos/zalando/kontrolletti/notifications{?since,all,participating}",
      "labels_url" : "https://api.github.com/repos/zalando/kontrolletti/labels{/name}",
      "releases_url" : "https://api.github.com/repos/zalando/kontrolletti/releases{/id}",
      "created_at" : "2015-03-30T14:24:38Z",
      "updated_at" : "2015-08-04T12:16:41Z",
      "pushed_at" : "2015-08-04T08:08:56Z",
      "git_url" : "git://github.com/zalando/kontrolletti.git",
      "ssh_url" : "git@github.com:zalando/kontrolletti.git",
      "clone_url" : "https://github.com/zalando/kontrolletti.git",
      "svn_url" : "https://github.com/zalando/kontrolletti",
      "homepage" : "https://zalando.github.io/kontrolletti",
      "size" : 2739,
      "stargazers_count" : 1,
      "watchers_count" : 1,
      "language" : "JavaScript",
      "has_issues" : true,
      "has_downloads" : true,
      "has_wiki" : true,
      "has_pages" : true,
      "forks_count" : 0,
      "mirror_url" : null,
      "open_issues_count" : 14,
      "forks" : 0,
      "open_issues" : 14,
      "watchers" : 1,
      "default_branch" : "develop",
      "organization" : {
        "login" : "zalando",
        "id" : 1564818,
        "avatar_url" : "https://avatars.githubusercontent.com/u/1564818?v=3",
        "gravatar_id" : "",
        "url" : "https://api.github.com/users/zalando",
        "html_url" : "https://github.com/zalando",
        "followers_url" : "https://api.github.com/users/zalando/followers",
        "following_url" : "https://api.github.com/users/zalando/following{/other_user}",
        "gists_url" : "https://api.github.com/users/zalando/gists{/gist_id}",
        "starred_url" : "https://api.github.com/users/zalando/starred{/owner}{/repo}",
        "subscriptions_url" : "https://api.github.com/users/zalando/subscriptions",
        "organizations_url" : "https://api.github.com/users/zalando/orgs",
        "repos_url" : "https://api.github.com/users/zalando/repos",
        "events_url" : "https://api.github.com/users/zalando/events{/privacy}",
        "received_events_url" : "https://api.github.com/users/zalando/received_events",
        "type" : "Organization",
        "site_admin" : false
      },
      "network_count" : 0,
      "subscribers_count" : 7
    }
    """
  val stashRepo = """
    {
      "slug" : "ci-cd",
      "id" : 2578,
      "name" : "ci-cd",
      "scmId" : "git",
      "state" : "AVAILABLE",
      "statusMessage" : "Available",
      "forkable" : false,
      "project" : {
        "key" : "DOC",
        "id" : 981,
        "name" : "Dockerfiles",
        "public" : true,
        "type" : "NORMAL",
        "link" : {
          "url" : "/projects/DOC",
          "rel" : "self"
        },
        "links" : {
          "self" : [ {
            "href" : "https://stash.zalando.net/projects/DOC"
          } ]
        }
      },
      "public" : true,
      "link" : {
        "url" : "/projects/DOC/repos/ci-cd/browse",
        "rel" : "self"
      },
      "cloneUrl" : "https://stash.zalando.net/scm/doc/ci-cd.git",
      "links" : {
        "clone" : [ {
          "href" : "https://stash.zalando.net/scm/doc/ci-cd.git",
          "name" : "http"
        }, {
          "href" : "ssh://git@stash.zalando.net:7999/doc/ci-cd.git",
          "name" : "ssh"
        } ],
        "self" : [ {
          "href" : "https://stash.zalando.net/projects/DOC/repos/ci-cd/browse"
        } ]
      }
    }
    """
  
  val singleGithubCommit = """
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
          "url": "https://api.github.com/repos/zalando/kontrolletti/git/trees/6336b59c25540296d4d6d718fbee2480403e756f"
        },
        "url": "https://api.github.com/repos/zalando/kontrolletti/git/commits/50cea1156ca558eb6c67e78ca7e5dabc570ea99a",
        "comment_count": 0
      },
      "url": "https://api.github.com/repos/zalando/kontrolletti/commits/50cea1156ca558eb6c67e78ca7e5dabc570ea99a",
      "html_url": "https://github.com/zalando/kontrolletti/commit/50cea1156ca558eb6c67e78ca7e5dabc570ea99a",
      "comments_url": "https://api.github.com/repos/zalando/kontrolletti/commits/50cea1156ca558eb6c67e78ca7e5dabc570ea99a/comments",
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
          "url": "https://api.github.com/repos/zalando/kontrolletti/commits/88c31c976507b32574bb9c76311da1cfc4832d1d",
          "html_url": "https://github.com/zalando/kontrolletti/commit/88c31c976507b32574bb9c76311da1cfc4832d1d"
        },
        {
          "sha": "2ead1df4182c33bbca16768e4200a09ce3b6e68d",
          "url": "https://api.github.com/repos/zalando/kontrolletti/commits/2ead1df4182c33bbca16768e4200a09ce3b6e68d",
          "html_url": "https://github.com/zalando/kontrolletti/commit/2ead1df4182c33bbca16768e4200a09ce3b6e68d"
        }
      ],
      "stats": {
        "total": 869,
        "additions": 579,
        "deletions": 290
      }
    }
    """
    val singleStashCommit = """
      {
        "id": "644a78e681cab53c5cc27be0d3c6e338b9e64b3d",
        "displayId": "644a78e681c",
        "author": {
          "name": "Fernando Benjamin",
          "emailAddress": "benibadboy@hotmail.com"
        },
        "authorTimestamp": 1434466353000,
        "message": "Adding sbt-0.13.8.deb pacakge file back into repo",
        "parents": [
          {
            "id": "efa9834bfec4763792c1c6e2ef172ce2b766aff4",
            "displayId": "efa9834bfec",
            "author": {
              "name": "Fernando Benjamin",
              "emailAddress": "benibadboy@hotmail.com"
            },
            "authorTimestamp": 1434466329000,
            "message": "Adding sbt-0.13.8.deb pacakge file back into repo",
            "parents": [
              {
                "id": "2aded5f6843a87ec3720c1bee78b87a4025d1a0a",
                "displayId": "2aded5f6843"
              }
            ]
          }
        ]
      }
      """

}