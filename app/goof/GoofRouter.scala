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
        case GET(p"/") => controller.list // Without any prompt, send the list of things in the set

        case POST(p"/$arg") => controller.add(arg.toInt) // Let them post a number into the set

        case GET(p"/$arg") => controller.get(arg.toInt) // Tell them if the number is in the set
    }
}

