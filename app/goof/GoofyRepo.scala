package goof

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import scala.collection.mutable.Set // Everyone likes immutable stuff but I actually want to modify this one
import scala.collection.mutable.BitSet
import scala.collection.mutable.HashMap
import scala.concurrent.{ExecutionContext, Future}
import anorm._

// As it turns out, using the repository dispatcher again is probably a good idea
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
    private val gaffs: HashMap[Int,Set[Int]] = new HashMap(); // This time all the values will come from POSTs

    override def list(id: Int): Future[Iterable[Int]] = {
        Future { gaffs(id) } // now list retrieves the Set associated with the key
    }

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
                gaffs += (id -> new BitSet(num)) // add the id as a key pointing to a new set
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

/* Persistent implementation of GoofyRepo. 
 * After consideration, I've decided to store the data in a two-column table of Set IDs and the stringification of their values. If the values had an upper bound I would use a finite number of Longs
 */
@Singleton
class GoofyRepoSQL @Inject()(db: DataBase, implicit ec: GoofyExecutionContext) extends GoofyRepo {
    // Implicit converter to turn the strings from the database into BitSets for manipulation. Not always used implicitly
    implicit val unstringify: String => BitSet = s => {
        val nums = s.split(" ").map(_.toInt) // Numbers in string as array
        BitSet.empty ++ nums
    }

    private def stringify(set: Set): String = {
       set,mkstring(" ")
    } 

    protected def getSet(id: Int): Option[BitSet] = {
        db.withConnection {
            val str: Option[String] = SQL"""
                select nums from sets where id=$id
                """.as(SQLParser.str("nums").singleOpt)
        }
        str.map(unstringify(_)) // May have problematic type issues where the None[String] doesn't map into a None[BitSet], but I think None does not have a type
    }

    override def list(id: Int): Future[Iterable[Int]] = {
        Future {
            getSet(id) match {
                case Some(set) => set
                case None => BitSet.empty
            }
        }
    }

    override def add(id: Int, num: Int): Future[Option[Int]] = {
        Future {
            getSet(id).map( _ match {
                case Some(set) => 
                    if(set(num)) {
                        None // If the number is there, we don't add it
                    } else {
                        val str = stringify(set + num)
                        db.withConnection {
                            SQL""" update sets set nums=$str where id=$id """
                        }
                        Some(num) // Return the num if it was added
                    }
                case None =>
                    db.withConnection {
                        SQL""" insert into sets values ($id,$num) """ // Create the set if it doesn't exist
                    }
                    Some(num)
            }
        }
    }

    override def get(id: Int, num: Int): Future[Option[Int]] = {
        Future {
            getSet(id).map( if(_(num)) Some(num) else None ).flatten
        }
    }
}
