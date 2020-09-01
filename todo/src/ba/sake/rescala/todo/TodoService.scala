package ba.sake.rescala.todo

import java.util.UUID

import rescala.default._

object TodoService {

  val todos$ : Var[List[Todo]] = Var(
    List(Todo("Create a TodoMVC template", completed = true), Todo("Rule the web"))
  )

  private val toggleAllState = Var(false)

  def add(todo: Todo): Unit =
    todos$.set(
      todos$.now.appended(todo)
    )

  def remove(id: UUID): Unit =
    todos$.set(
      todos$.now.filterNot(_.id == id)
    )

  def removeCompleted(): Unit =
    todos$.set(
      todos$.now.filterNot(_.completed)
    )

  def toggleCompleted(todo: Todo): Unit =
    todos$.set {
      todos$.now.map(t => if (t.id == todo.id) todo.toggled else t)
    }

  def toggleAll(): Unit = {
    toggleAllState.set(!toggleAllState.now)
    todos$.set(
      todos$.now.map(_.copy(completed = toggleAllState.now))
    )
  }
}
