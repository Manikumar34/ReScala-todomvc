package ba.sake.rescala.todo

import org.scalajs.dom.ext.KeyValue
import org.scalajs.dom.raw.KeyboardEvent
import rescala.default._
import rescala.extra.Tags._
import scalatags.JsDom.all._
import TodoService.todos$

case class TodoComponent(
    todo: Todo
) {
  private val todoVar = Var(todo)
  private val isEdit  = Var(false)

  private val startEditingEvent    = Evt[Unit]()
  private val stopEditingEvent     = Evt[Unit]()
  private val toggleCompletedEvent = Evt[Unit]()

  startEditingEvent.observe(_ => startEditing())
  stopEditingEvent.observe(_ => stopEditing())
  toggleCompletedEvent.observe(_ => TodoService.toggleCompleted(todo))

  private val editInput = input(
    onblur := { () =>
      stopEditingEvent.fire(())
    },
    onkeyup := { (e: KeyboardEvent) =>
      if (e.key == KeyValue.Enter)
        stopEditingEvent.fire(())
    },
    value := todoVar.map(_.name),
    cls := "edit"
  ).render

  def render = {
    val todoName$    = Signal(span(todoVar().name))
    val isChecked    = Option.when(todo.completed)("checked")
    val completedCls = Option.when(todo.completed)("completed")
    val editingCls   = isEdit.map(v => Option.when(v)("editing"))
    val liClasses    = Signal { completedCls.getOrElse("") + " " + editingCls().getOrElse("") }

    li(cls := liClasses)(
      div(cls := "view")(
        input(checked := isChecked, onchange := { () =>
          toggleCompletedEvent.fire()
        }, cls := "toggle", tpe := "checkbox"),
        label(ondblclick := { () =>
          startEditingEvent.fire()
        })(todoName$.asModifier),
        button(onclick := { () =>
          TodoService.remove(todo.id)
        }, cls := "destroy")
      ),
      editInput
    )
  }

  private def startEditing(): Unit = {
    isEdit.set(true)
    editInput.focus()
    editInput.selectionStart = editInput.value.length
  }

  private def stopEditing(): Unit = {
    isEdit.set(false)
    todos$.set {
      val todos    = todos$.now
      val newValue = editInput.value.trim
      if (newValue.isEmpty) todos
      else {
        todoVar.set(todoVar.now.copy(name = newValue))
        val todoIdx = todos.indexWhere(_.id == todo.id)
        todos.updated(todoIdx, todoVar.now)
      }
    }
  }
}
