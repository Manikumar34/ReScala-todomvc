package ba.sake.rescala.todo

import org.scalajs.dom.ext.KeyValue
import org.scalajs.dom.html.LI
import org.scalajs.dom.raw.KeyboardEvent
import rescala.default._
import rescala.extra.Tags._
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._

case class TodoComponent(
    todo: Todo
) {
  private val todos$ = TodoService.todos$

  private val todoVar = Var(todo)
  private val isEdit  = Var(false)

  private val startEditingEvent    = Evt[Unit]()
  private val stopEditingEvent     = Evt[Option[KeyboardEvent]]()
  private val toggleCompletedEvent = Evt[Unit]()

  private val editInput = input(
    onblur := { () =>
      stopEditingEvent.fire(None)
    },
    onkeyup := { (e: KeyboardEvent) =>
      stopEditingEvent.fire(Some(e))
    },
    value := todoVar.map(_.name),
    cls := "edit"
  ).render

  def render: TypedTag[LI] = {
    startEditingEvent.observe(_ => startEditing())
    stopEditingEvent.observe(stopEditing)
    toggleCompletedEvent.observe(_ => TodoService.toggleCompleted(todo))

    val todoName$    = Signal(span(todoVar().name))
    val checkedAttr  = Option.when(todo.completed)("checked")
    val completedCls = Option.when(todo.completed)("completed")
    val editingCls   = isEdit.map(v => Option.when(v)("editing"))
    val liClasses = Signal { completedCls.getOrElse("") + " " + editingCls().getOrElse("") }

    li(cls := liClasses)(
      div(cls := "view")(
        input(checked := checkedAttr, onchange := { () =>
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

  private def stopEditing(maybeKbdEvt: Option[KeyboardEvent]): Unit = {
    if (maybeKbdEvt.nonEmpty && maybeKbdEvt.get.key != KeyValue.Enter) return
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
