package goof

trait GoofyServices {
    def sum(id: Int): Int
    def product(id: Int): Int
    def max(id: Int): Int
    def min(id: Int): Int
}

class GoofyServicesImpl @Inject()(repo: GoofyRepo) extends GoofyServices {
    private def onSet[A](id: Int)(fun: Future[Iterable[Int]] => A): A = {
        repo.list(id).map( fun(_) )
    }

    // There's definitely some error-handling to do with empty lists here, but I really just want to demonstrate unit tests so I won't bother
    override def sum(id: Int): Int = { onSet(id)(_.sum) }
    override def product(id: Int): Int = { onSet(id)(_.product) }
    override def max(id: Int): Int = { onSet(id)(_.max) }
    override def min(id: Int): Int = { onSet(id)(_.min) }
}
