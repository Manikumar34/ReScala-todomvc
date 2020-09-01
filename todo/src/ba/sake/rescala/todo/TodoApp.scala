package ba.sake.rescala.todo

import org.scalajs.dom

object TodoApp {

  def main(args: Array[String]): Unit =
    dom.document.body.appendChild(MainComponent.render)
}
