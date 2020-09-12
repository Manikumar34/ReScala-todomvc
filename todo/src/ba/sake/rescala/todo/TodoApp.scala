package ba.sake.rescala.todo

import ba.sake.scalajs_router.Router
import org.scalajs.dom
import rescala.default._

object TodoApp {

  def main(args: Array[String]): Unit = {
    dom.document.getElementById("main").appendChild(MainComponent.render)

    Router().withListener {
      case "/active"    => MainComponent.todoFilter.set(TodoFilter.Active)
      case "/completed" => MainComponent.todoFilter.set(TodoFilter.Completed)
      case _            => MainComponent.todoFilter.set(TodoFilter.All)
    }
  }
}
