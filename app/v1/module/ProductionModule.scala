package v1.module

import com.google.inject.AbstractModule

import v1.service.SearchService
import v1.service.SearchServiceImpl


class ProductionModule extends AbstractModule {
  def configure() {
    bind(classOf[SearchService]).to(classOf[SearchServiceImpl])
    //bind(classOf[SCMClient]).to(classOf[ClientWrapper])
    
  }
}