package ba.sake.rescala.todo

import java.util.UUID
import org.scalajs.dom
import rescala.default._
import upickle.default._

object TodoService {
  private val TodosKey = "TODOS"

  private val toggleAllState = Var(false)

  val todos$ : Var[List[Todo]] = initTodos()

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

  private def initTodos() = {
    val savedTodosJson = dom.window.localStorage.getItem(TodosKey)
    val todos =
      if (savedTodosJson == null)
        List(Todo("Create a TodoMVC template", completed = true), Todo("Rule the web"))
      else read[List[Todo]](savedTodosJson)

    val initTodos$ = Var(todos)
    initTodos$.observe { newValue =>
      dom.window.localStorage.setItem(TodosKey, write(newValue))
    }
    initTodos$
  }
}
