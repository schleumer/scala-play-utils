package sch.utils

import play.api.i18n._
import controllers._
import play.api._
import play.api.mvc._
import play.api.mvc.Results
import controllers._
import play.api._
import play._
import sch._
import play.api.libs.iteratee._

import play.api.mvc._

class Lollipop(request: Request[Any], lang: Option[String]) {

  /**
   * Only the dead can know peace from this evil
   */
  val uuid: String = request.session.get("vidaloka").getOrElse(java.util.UUID.randomUUID().toString + "/" + java.util.UUID.randomUUID().toString + "/" + java.util.UUID.randomUUID().toString)

  val session: Session = new Session(uuid)

  /**
   * TODO: tirar isso daqui
   * Pega o tema ~inutil~ o suficiente para não estar aqui
   * @return
   */
  def theme(): String = request.session.get("theme").getOrElse("default")

  def ok(t: play.api.templates.Html): play.api.mvc.SimpleResult = beforeResult(Results.Ok(t))

  def ok(t: String): play.api.mvc.SimpleResult = beforeResult(Results.Ok(t))

  def beforeResult(res: play.api.mvc.SimpleResult): play.api.mvc.SimpleResult = res.withSession(request.session +("vidaloka", uuid))

  /**
   * TODO: tirar isso daqui
   * gettext simples
   * @param c
   * @return
   */
  def str(c: String): String = Messages(c)(Lang(lang.getOrElse[String]("en")))

  /**
   * TODO: tirar isso daqui
   * gettext basico com grupo
   * @param c
   * @param c2
   * @return
   */
  def str(c: String, c2: Any*): String = Messages(c, c2: _*)(Lang(lang.getOrElse[String]("en")))

}

trait LollipopHelper {
  implicit def lollipop[A](implicit request: Request[A]): Lollipop = {
    new Lollipop(
      request,
      Some(request.session.get("lang").getOrElse("pt-BR"))
    )
  }
}