package goof

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import scala.collection.mutable.Set // Everyone likes immutable stuff but I actually want to modify this one
import scala.colection.mutable.HashMap
import scala.concurrent.{ExecutionContext, Future}

class GoofyExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "repository.dispatcher")

// Interface for my goofy backend, which is not a Set anymore
trait GoofyRepo {
    def list(id: Int): Future[Iterable[Int]] // List off the values in the Set
    def add(id: Int, num: Int): Future[Option[Int]] // Add a value to the Set, returning the value added, or None if it was already in there
    def get(id: Int, num: Int): Future[Option[Int]] // Get a value from the Set, or None if it is not inside
}

@Singleton // One and only one of these
class GoofyRepoImpl @Inject()(implicit ec: GoofyExecutionContext) extends GoofyRepo { 
    // I can't figure out how the example adds new posts even though all the types are immutable, so I'm using a mutable
    // This may cause problems with the highly asynchronous nature of Play (eg: race conditions)
    // Gaffs is now a HashMap of Sets. I didn't choose a Set of Sets because then I wouldn't have an index to a particular value.
    private val gaffs: HashMap[Set[Int]] = new HashMap(); // This time all the values will come from POSTs

    override def list(id: int): Future[Iterable[Int]] = {
        Future { gaffs(id) } // now list retrieves the Set associated with the key
    }

    // I was going to use fancy pattern-matching for this, but research suggested if/else was better if you don't already have Options
    override def add(id: Int, num: Int): Future[Option[Int]] =  {
        Future {
            if(gaffs.contains(id)) { // check if the set they want already exists
                val set = gaffs(id);
                if(set.contains(num)) {
                    None // if the number is already in, return None
                } else {
                    set += num // add the num in
                        Some(num) // return it, signaling it was added
                }
            } else { // if the Set isn't in there already
                gaffs += (id -> new Set(num)) // add the id as a key pointing to a new set
                    Some(num)
            }
        }
    }
    
    override def get(id: Int, num: Int): Future[Option[Int]] = {
        Future {
            if(gaffs.contains(id) && gaffs(id)(num)) {
                Some(num)
            } else {
                None
            }
        }
    }
}

        
