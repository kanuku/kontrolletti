package v1.module

import com.google.inject.AbstractModule
import v1.client.SCM
import v1.client.Github
import v1.service.SearchImpl
import v1.service.Search

class Production extends AbstractModule {
  def configure() {
    bind(classOf[Search]).to(classOf[SearchImpl])
    bind(classOf[SCM]).to(classOf[Github])

  }
}