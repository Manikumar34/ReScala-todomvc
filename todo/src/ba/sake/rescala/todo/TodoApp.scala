package ba.sake.rescala.todo

import java.util.UUID

import org.scalajs.dom
import org.scalajs.dom.ext.KeyValue
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.KeyboardEvent
import rescala.default._
import rescala.extra.Tags._
import scalatags.JsDom.all._

/*
<input>s are decoupled from its callbacks:
- fire Evt
- react to Evt
*/

case class Todo(name: String, completed: Boolean = false, id: UUID = UUID.randomUUID()) {
  def toggled: Todo = copy(completed = !completed)
}

object TodoApp {

  private val section = tag("section")

  def main(args: Array[String]): Unit = {

    val todos$ = Var(List(Todo("Create a TodoMVC template", completed = true), Todo("Rule the web")))
    val toggleAllState = Var(false)

    /* ADD */
    val addTodoEvent = Evt[KeyboardEvent]()
    val addInput = input(onkeyup := { (e: KeyboardEvent) => addTodoEvent.fire(e) },
      cls := "new-todo", placeholder := "What needs to be done?", autofocus).render

    def addTodo(e: KeyboardEvent): Unit = {
      val newTodoName = e.target.asInstanceOf[Input].value.trim
      if (e.key == KeyValue.Enter && newTodoName.nonEmpty) {
        todos$.set(
          todos$.now :+ Todo(newTodoName)
        )
        addInput.value = ""
      }
    }

    addTodoEvent.observe(addTodo)

    /* CLEAR COMPLETED */
    def clearCompleted(): Unit = {
      todos$.set(
        todos$.now.filterNot(_.completed)
      )
    }

    def toggleAll(): Unit = {
      toggleAllState.set(!toggleAllState.now)
      todos$.set(
        todos$.now.map(_.copy(completed = toggleAllState.now))
      )
    }

    def todoItem(todo: Todo) = {
      val todoVar = Var(todo)
      val isEdit = Var(false)

      val startEditingEvent = Evt[Unit]()
      val stopEditingEvent = Evt[Option[KeyboardEvent]]()
      val toggleCompletedEvent = Evt[Unit]()

      val editInput = input(
        onblur := { () => stopEditingEvent.fire(None) },
        onkeyup := { (e: KeyboardEvent) => stopEditingEvent.fire(Some(e)) },
        value := todoVar.map(_.name),
        cls := "edit"
      ).render

      def removeTodo(): Unit =
        todos$.set(
          todos$.now.filterNot(_.id == todo.id)
        )

      def startEditing(): Unit = {
        isEdit.set(true)
        editInput.focus()
        editInput.selectionStart = editInput.value.length
      }

      def stopEditing(maybeKbdEvt: Option[KeyboardEvent]): Unit = {
        if (maybeKbdEvt.nonEmpty && maybeKbdEvt.get.key != KeyValue.Enter) return
        isEdit.set(false)
        todos$.set {
          val todos = todos$.now
          val newValue = editInput.value.trim
          if (newValue.isEmpty) todos
          else {
            todoVar.set(todoVar.now.copy(name = newValue))
            val todoIdx = todos.indexWhere(_.id == todo.id)
            todos.updated(todoIdx, todoVar.now)
          }
        }
      }

      def toggleCompleted(): Unit = todos$.set {
        todos$.now.map(t => if (t.id == todo.id) todo.toggled else t)
      }

      startEditingEvent.observe { _ => startEditing() }
      stopEditingEvent.observe(stopEditing)
      toggleCompletedEvent.observe { _ => toggleCompleted() }


      val todoName$ = Signal(span(todoVar().name))
      val completedCls = Option.when(todo.completed)("completed")
      val checkedAttr = Option.when(todo.completed)("checked")
      val editingCls = isEdit.map { v => Option.when(v)("editing") }
      val liClasses = Signal {
        completedCls.getOrElse("") + " " + editingCls().getOrElse("")
      }

      li(cls := liClasses)(
        div(cls := "view")(
          input(checked := checkedAttr, onchange := { () => toggleCompletedEvent.fire() }, cls := "toggle", tpe := "checkbox"),
          label(ondblclick := { () => startEditingEvent.fire() })(todoName$.asModifier),
          button(onclick := { () => removeTodo() }, cls := "destroy")
        ),
        editInput
      )
    }

    val displayMainAndFooter = Signal {
      if (todos$().isEmpty) "none" else "block"
    }

    val bla = todos$.map { todos =>
      val count = todos.size
      val itemsLabel = if (count == 1) "item" else "items"
      frag(strong("0"), s"$itemsLabel left").render
    }

    val mainFrag = Seq(
      section(cls := "todoapp")(
        header(cls := "header")(
          h1("todos"),
          addInput
        ),
        section(cls := "main", css("display") := displayMainAndFooter)(
          input(onclick := { () => toggleAll() }, id := "toggle-all", cls := "toggle-all", tpe := "checkbox"),
          label(`for` := "toggle-all", "Mark all as complete"),
          ul(cls := "todo-list")(
            todos$.map {
              _.map(todoItem)
            }.asModifierL
          )
        ),
        footer(cls := "footer", css("display") := displayMainAndFooter)(
          // This should be `0 items left` by default
          span(cls := "todo-count")(
            todos$.map { todos =>
              val count = todos.count(!_.completed)
              val itemsLabel = if (count == 1) "item" else "items"
              div(strong(count), s" $itemsLabel left")
            }.asModifier
          ),
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
          button(onclick := { () => clearCompleted() }, cls := "clear-completed", "Clear completed")
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
