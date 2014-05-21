package sch.utils

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.iteratee._
import play.api.libs.json._
import scala.collection.mutable

package object Taskable {
  type FutureText = Future[Option[String]]

  def FutureText(arg: Option[String]) = Future[Option[String]](arg)

  type TaskStack = mutable.Stack[() => FutureText]

  def TaskStack(tasks: (() => FutureText)*) = mutable.Stack[() => FutureText](tasks: _*)

  type Task = () => Future[Option[String]]

  def Task(task: Option[String]) = () => Future[Option[String]] {
    task
  }

  object Tasks {
    def js(data: (String, String)): Option[String] = {
      Some(Json.stringify(Json.obj("type" -> data._1, "message" -> data._2)))
    }

    def apply(tasks: Taskable.TaskStack): Enumerator[String] = {
      var taskTable = tasks
      Enumerator.generateM {
        if (taskTable.length < 1) {
          Future {
            None
          }
        } else {
          val task = taskTable.head
          taskTable = taskTable.drop(1)
          val promised = Promise[Option[String]]()
          val t = task()
          t onFailure {
            case e => promised.success(js("error", "Eeeeeeepa, algum erro aconteceu."))
          }
          t map { r =>
            promised.success(r)
          }
          promised.future
        }
      }
    }
  }

}