package controllers

import elasticsearch.model.JsonModel
import play.api.mvc.Action
import play.api.mvc.Controller
import elasticsearch.Client
import play.api.libs.json._

object Repository extends Controller with JsonModel {

  def list = Action {
     Ok( Json.prettyPrint(Json.toJson(Client.repositories)))
  }

}