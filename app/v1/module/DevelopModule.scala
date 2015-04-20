package v1.module

import com.google.inject.AbstractModule
import v1.service.SearchServiceImpl
import v1.service.SearchService

class DevelopModule extends AbstractModule {

  def configure() {
    bind(classOf[SearchService]).to(classOf[SearchServiceImpl])
  }
}