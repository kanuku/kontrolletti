package v1.module
import org.scalatest.FreeSpec
import com.google.inject.AbstractModule
import v1.client.SCMClient
import v1.client.ClientWrapper
import v1.client.ClientWrapper
import v1.service.SearchServiceImpl
import v1.service.SearchService
import com.google.inject.Guice

trait TestModule {
  object Composition extends AbstractModule {
    def configure = {
      println("test")
      bind(classOf[SearchService]).to(classOf[SearchServiceImpl])
      bind(classOf[SCMClient]).to(classOf[ClientWrapper])
    }
  }
  
  
  
}
 