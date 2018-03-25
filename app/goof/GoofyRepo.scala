package goof

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import scala.collection.mutable.Set // Everyone likes immutable stuff but I actually want to modify this one
import scala.concurrent.{ExecutionContext, Future}

// We'll pretend this does heavy processing and use a custom thread pool. Idk what "repository.dispatcher" does, and the documentation is not helping.
// I'm just going to hope leaving it this way causes no major problems.
class GoofyExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "repository.dispatcher")

// Interface for my goofy backend, which is really just a Set
trait GoofyRepo {
    def list(): Future[Iterable[Int]] // List off the values in the Set
    def add(num: Int): Future[Option[Int]] // Add a value to the Set, returning the value added, or None if it was already in there
    def get(num: Int): Future[Option[Int]] // Get a value from the Set, or None if it is not inside
}

@Singleton // One and only one of these
class GoofyRepoImpl @Inject()(implicit ec: GoofyExecutionContext) extends GoofyRepo { 
    // I can't figure out how the example adds new posts even though all the types are immutable, so I'm using a var.
    // This may cause problems with the highly asynchronous nature of Play (eg: race conditions)
    private val gaffs: Set[Int] = Set(1, 2, 3, 4, 5) // Intialize with some values. More will be added by POST requests

    override def list(): Future[Iterable[Int]] = {
        Future { gaffs }
    }
    // I was going to use fancy pattern-matching for this, but research suggested if/else was better if you don't already have Options
    override def add(num: Int): Future[Option[Int]] =  {
        Future {
                if(gaffs(num)) {
                    None // if the number is already in, return None
                } else {
                    gaffs += num // add the num in
                    Some(num) // return it, signaling it was added
                }
            }
        }
    

    override def get(num: Int): Future[Option[Int]] = {
        Future {
            if(gaffs(num)) { Some(num) } else { None } // tell if the num was in there
        }
    }
}

        
