package filter

import javax.inject.Inject
import play.api.http.HttpFilters

class DefaultFilter @Inject() (oauth2Filter: OAuth2Filter) extends HttpFilters {

  val filters = Seq(oauth2Filter)
}