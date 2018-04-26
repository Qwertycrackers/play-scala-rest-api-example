package goof 

import javax.inject.Inject 

import play.api.mvc._
import play.api.libs.json._
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global

class GoofyController @Inject()(repo: GoofyRepo) extends Controller { 
    private val logger = Logger(getClass) // not certain how getClass works but I want to log things

    implicit val implicitJsInt = new Writes[Int] {
        def writes(num: Int): JsValue = {
            Json.obj("num" -> num)
        }
    } // now we can write our ints out as Json in a field of a name we chose

    // I'll implement these in terms of Futures because that seems to be the way people prefer, although Play claims to be non-blocking always
    // I'm not implementing my own ActionBuilder, though, because I don't think I need any of the things gained from it
    def list = Action.async { implicit request =>
        logger.trace("List: ") // log that we received a request for the list
        repo.list().map(set => Ok(Json.toJson(set.toSeq))) // Make the list into a Sequence and then into a JsArray, sending back the the Ok result
    }
    
    def add(num: Int) = Action.async { implicit request =>
        logger.trace(s"Adding $num")
        val sometimeNum = repo.add(num)
        sometimeNum.map( _ match {
            case Some(num) => Ok(Json.toJson(num))
            case _ => NotFound(Json.toJson("Already in"))
        })
    }
    // This may seem like duplicate code to the above, but is really is only so for now, so it doesn't make sense to merge them. 
    def get(num: Int) = Action.async { implicit request =>
        logger.trace(s"Getting $num")
        val sometimeNum = repo.get(num)
        sometimeNum.map( _ match {
            case Some(num) => Ok(Json.toJson(num));
            case _ => NotFound(Json.toJson("Wasn't Found"));
        })
    }
}

