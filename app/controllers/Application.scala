package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def swagger =  Action {
      Ok(views.html.swagger())
  }

}