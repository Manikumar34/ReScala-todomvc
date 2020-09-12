package ba.sake.rescala.todo

import java.util.UUID

import rescala.default._

// TODO persist to localstorage
object TodoService {

  val todos$ : Var[List[Todo]] = Var(
    List(Todo("Create a TodoMVC template", completed = true), Todo("Rule the web"))
  )

  private val toggleAllState = Var(false)

  def add(todo: Todo): Unit =
    todos$.transform(_.appended(todo))

  def remove(id: UUID): Unit =
    todos$.transform(_.filterNot(_.id == id))

  def removeCompleted(): Unit =
    todos$.transform(_.filterNot(_.completed))

  def toggleCompleted(todo: Todo): Unit =
    todos$.transform {
      _.map(t => if (t.id == todo.id) todo.toggled else t)
    }

  def toggleAll(): Unit = {
    toggleAllState.transform(s => !s)
    todos$.transform(
      _.map(_.copy(completed = toggleAllState.now))
    )
  }
}
