package v1.model

/**
 * The models
 *
 */

//case class Repository(name: String, resource: Resource, url: String, commits: List[Commit])
//case class Commit(id: String, message: String, committer: User)
//case class Resource(name: String, url: String)
case class Author(name: String, email: String)
case class Commit(id: String, message: String, valid: Boolean, author: Author)

 