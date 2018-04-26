package goof 

import javax.inject.Inject 

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class GoofRouter @Inject()(controller: GoofyController) extends SimpleRouter {
    // First example will accept POST requests, extract number, and put them in a Set. Attempting to Get will tell you if a number is in the set.
    // My understanding is that `routes` uses pattern-matching to select the path the request shall be sent down, and the Router serves only to choose
    // which Action to invoke in the controller
    override def routes: Routes = {
        case GET(p"/$id") => controller.list(id.toInt) // Now lists the numbers which are in the Set at $id

        case POST(p"/$id/$arg") => controller.add(id.toInt, arg.toInt) // post a number to that set

        case GET(p"/$id/$arg") => controller.get(id.toInt, arg.toInt) // check if a number is in that set
    }
}

