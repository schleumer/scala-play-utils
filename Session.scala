package sch.utils

import java.io._
import scala.io.Source
import play._
import scala.collection.mutable._
import play.api.libs.json._
import play.api.libs.json.Reads._
import anorm._
import play.api.db.DB
import play.api.Play.current
import com.github.nscala_time.time.Imports._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.async.Async.{async, await}	
import com.redis._

class Session(uuid: String) {
	val redis = new RedisClient("localhost", 6379)

	var _data:JsValue = Json.obj()
	var fetched = false

	def put(key: String, value: JsValue) {
		fetch()
		var apath = key.split('.')

		if(apath.length < 1){
			throw new Exception("NO WAY!")
		}

		var path:JsPath = (__)
		var branch = apath.last
		apath = apath.dropRight(1)

		apath.map { p =>
			path = (path \ p)
		}

		val transformer = ( path ).json.update( of[JsObject].map { o => o ++ Json.obj(branch -> value) } )

		_data = _data.transform(transformer).getOrElse(_data)
		save()
	}

	def <<(obj: (String, JsValue)): Session = {
		put(obj._1, obj._2)
		this
	}

	def get(): JsValue = {
		fetch()
		_data
	}

	def get(key: String): JsValue = {
		fetch()
		var value = _data
		key.split('.').map { e =>
			value = (value \ e)
		}
		value
	}

	def has(key: String): Boolean = {
		fetch()
		var value = _data
		key.split('.').map { e =>
			value = (value \ e)
		}
		!value.isInstanceOf[JsUndefined]
	}

	def fetch() {
		if(!fetched){
			fetched = true
			var datass = redis.get("play.sessions." + uuid)
			try{
				_data = Json.parse(datass.getOrElse("{}"))
			} catch {
				case e: Throwable => {
					_data = Json.obj()
					redis.del("play.sessions." + uuid)	
				}
			}
		}
	}

	def save() {
		// Salva o arquivo e deixa as treta para o futuro, porque o usuário quer saber do resultado e não se o arquivo foi salvo
		// Caso haja treta o fetch resolvera recriando a sessão
		redis.set("play.sessions." + uuid, Json.stringify(_data))
	}

	def kill() {
		redis.del("play.sessions." + uuid)	
	}

}

object Session {
	def apply(uuid: String): Session = {
		return new Session(uuid)
	}
}