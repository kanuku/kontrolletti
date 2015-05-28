package module

import com.google.inject.AbstractModule

import client.SCM
import client.SCMImpl
import service.Search
import service.SearchImpl

class Develop extends AbstractModule {

  def configure() {
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[SCM]).to(classOf[SCMImpl])

  }
}