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

  private val startEditingEvent = Evt[Unit]()
  startEditingEvent.observe(_ => startEditing())

  private val stopEditingEvent = Evt[Unit]()
  stopEditingEvent.observe(_ => stopEditing())

  private val toggleCompletedEvent = Evt[Unit]()
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
    val todoName$    = Signal(span(todoVar().name)).asModifier
    val isChecked    = Option.when(todo.completed)("checked")
    val completedCls = Option.when(todo.completed)("completed")
    val editingCls   = isEdit.map(v => Option.when(v)("editing"))
    val liClasses    = Signal { completedCls.getOrElse("") + " " + editingCls().getOrElse("") }

    li(cls := liClasses)(
      div(cls := "view")(
        input(onchange := { () =>
          toggleCompletedEvent.fire()
        }, checked := isChecked, cls := "toggle", tpe := "checkbox"),
        label(ondblclick := { () =>
          startEditingEvent.fire()
        })(todoName$),
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
    todos$.transform { todos =>
      val newValue = editInput.value.trim
      if (newValue.isEmpty) todos
      else {
        todoVar.transform(_.copy(name = newValue))
        val todoIdx = todos.indexWhere(_.id == todo.id)
        todos.updated(todoIdx, todoVar.now)
      }
    }
  }
}
