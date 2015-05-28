package module

import com.google.inject.AbstractModule
import service.SearchImpl
import service.Search
import client.SCM
import client.SCMImpl

class Production extends AbstractModule {
  def configure() {
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[SCM]).to(classOf[SCMImpl])

  }
}