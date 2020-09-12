package ba.sake.rescala.todo

import ba.sake.rescala.todo.TodoService.todos$
import org.scalajs.dom.ext.KeyValue
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.KeyboardEvent
import rescala.default._
import rescala.extra.Tags._
import scalatags.JsDom.all._

// <input>s are decoupled from its callbacks via Events
object MainComponent {
  val todoFilter: Var[TodoFilter] = Var(TodoFilter.All)
  private val todosFiltered$      = todos$.map(todos => todos.filter(todoFilter().isValid))

  private val addTodoEvent = Evt[KeyboardEvent]()
  addTodoEvent.observe(addTodo)

  private val addInput = input(onkeyup := { (e: KeyboardEvent) =>
    if (e.key == KeyValue.Enter)
      addTodoEvent.fire(e)
  }, cls := "new-todo", placeholder := "What needs to be done?", autofocus).render

  private val mainDisplay = Signal { if (todos$().isEmpty) "none" else "block" }

  private val countFrag = todosFiltered$.map { todos =>
    val count      = todos.count(!_.completed)
    val itemsLabel = if (count == 1) "item" else "items"
    div(strong(count), s" $itemsLabel left")
  }.asModifier

  def render =
    div(
      tag("section")(cls := "todoapp")(
        header(cls := "header")(
          h1("todos"),
          addInput
        ),
        tag("section")(cls := "main", css("display") := mainDisplay)(
          input(onclick := { () =>
            TodoService.toggleAll()
          }, id := "toggle-all", cls := "toggle-all", tpe := "checkbox"),
          label(`for` := "toggle-all", "Mark all as complete"),
          ul(cls := "todo-list")(
            todosFiltered$.map {
              _.map(TodoComponent).map(_.render)
            }.asModifierL
          )
        ),
        footer(cls := "footer", css("display") := mainDisplay)(
          span(cls := "todo-count")(countFrag),
          ul(cls := "filters")(
            li(a(data.navigate := "/", "All", cls := maybeSelected(TodoFilter.All))),
            li(a(data.navigate := "/active", "Active", cls := maybeSelected(TodoFilter.Active))),
            li(a(data.navigate := "/completed", "Completed", cls := maybeSelected(TodoFilter.Completed)))
          ),
          button(onclick := { () =>
            TodoService.removeCompleted()
          }, cls := "clear-completed", "Clear completed")
        )
      ),
      footer(cls := "info")(
        p("Double-click to edit a todo"),
        p("Created by ", a(href := "https://sake.ba")("Sakib Hadžiavdić")),
        p("Part of ", a(href := "http://todomvc.com")("TodoMVC"))
      )
    ).render

  private def addTodo(e: KeyboardEvent): Unit = {
    val newTodoName = e.target.asInstanceOf[Input].value.trim
    if (newTodoName.nonEmpty) {
      TodoService.add(Todo(newTodoName))
      addInput.value = ""
    }
  }

  private def maybeSelected(filter: TodoFilter) = Signal {
    Option.when(todoFilter() == filter)("selected")
  }
}
