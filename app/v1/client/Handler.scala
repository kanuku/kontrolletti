package v1.client

import v1.model.Repository 
import v1.model.Commit
import v1.model.Resource


abstract case class GithubClient(protocol: String, baseUrl: String) extends Client {

}

abstract case class StashClient(protocol: String, baseUrl: String) extends Client {

}

