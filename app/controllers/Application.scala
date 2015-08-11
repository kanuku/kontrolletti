package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import scala.io.Source._


object Application extends Controller {
	private val file=scala.io.Source.fromFile(Play.application.getFile("conf/swagger.json")).mkString

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def swagger =  Action {
      Ok(views.html.swagger())
  }
  def status =  Action { 
      Ok
  }
  def specs = Action {
    Ok(file).as("application/json")
  }
  

}