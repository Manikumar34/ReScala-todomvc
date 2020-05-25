package ba.sake.rescala.todo

import org.scalajs.dom
import org.scalajs.dom.ext.KeyValue
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.KeyboardEvent
import rescala.default._
import rescala.extra.Tags._
import scalatags.JsDom.all._

// <input>s are decoupled from its callbacks via Events

object TodoApp {
  private val section = tag("section")
  private val todos$  = TodoService.todos$

  def main(args: Array[String]): Unit = {
    val addTodoEvent = Evt[KeyboardEvent]()
    val addInput = input(onkeyup := { (e: KeyboardEvent) =>
      addTodoEvent.fire(e)
    }, cls := "new-todo", placeholder := "What needs to be done?", autofocus).render

    def addTodo(e: KeyboardEvent): Unit = {
      val newTodoName = e.target.asInstanceOf[Input].value.trim
      if (e.key == KeyValue.Enter && newTodoName.nonEmpty) {
        TodoService.add(Todo(newTodoName))
        addInput.value = ""
      }
    }

    addTodoEvent.observe(addTodo)

    val displayMainAndFooter = Signal { if (todos$().isEmpty) "none" else "block" }

    val countFrag = todos$.map { todos =>
      val count      = todos.count(!_.completed)
      val itemsLabel = if (count == 1) "item" else "items"
      div(strong(count), s" $itemsLabel left")
    }.asModifier

    val mainFrag = Seq(
      section(cls := "todoapp")(
        header(cls := "header")(
          h1("todos"),
          addInput
        ),
        section(cls := "main", css("display") := displayMainAndFooter)(
          input(onclick := { () =>
            TodoService.toggleAll()
          }, id := "toggle-all", cls := "toggle-all", tpe := "checkbox"),
          label(`for` := "toggle-all", "Mark all as complete"),
          ul(cls := "todo-list")(
            todos$.map {
              _.map(TodoComponent).map(_.render)
            }.asModifierL
          )
        ),
        footer(cls := "footer", css("display") := displayMainAndFooter)(
          span(cls := "todo-count")(countFrag),
          // TODO
          /*
          ul(cls := "filters")(
            li(
              a(cls := "selected", href := "#/", "All")
            ),
            li(
              a(href := "#/active", "Active")
            ),
            li(
              a(href := "#/completed", "Completed")
            )
          ),
           */
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
    )
    dom.document.body.appendChild(mainFrag.render)
  }
}
