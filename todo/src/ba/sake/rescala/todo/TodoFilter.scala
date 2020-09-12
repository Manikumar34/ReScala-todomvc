package ba.sake.rescala.todo

sealed trait TodoFilter {
  def isValid(todo: Todo): Boolean
}

object TodoFilter {

  case object All extends TodoFilter {
    override def isValid(todo: Todo): Boolean = true
  }

  case object Completed extends TodoFilter {
    override def isValid(todo: Todo): Boolean = todo.completed
  }

  case object Active extends TodoFilter {
    override def isValid(todo: Todo): Boolean = !todo.completed
  }
}
