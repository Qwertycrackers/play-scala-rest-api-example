package goof

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

trait GoofyServices {
    def sum(id: Int): Future[Int]
    def product(id: Int): Future[Int]
    def max(id: Int): Future[Int]
    def min(id: Int): Future[Int]
}

class GoofyServicesImpl @Inject()(repo: GoofyRepo) extends GoofyServices {
    private def onSet[A](id: Int)(fun: Iterable[Int] => A): Future[A] = {
        repo.list(id).map( fun(_) )
    }

    // There's definitely some error-handling to do with empty lists here, but I really just want to demonstrate unit tests so I won't bother
    override def sum(id: Int): Future[Int] = { onSet(id)(_.sum) }
    override def product(id: Int): Future[Int] = { onSet(id)(_.product) }
    override def max(id: Int): Future[Int] = { onSet(id)(_.max) }
    override def min(id: Int): Future[Int] = { onSet(id)(_.min) }
}
