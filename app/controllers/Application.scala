package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Akka 
import play.api.Play.current
import actors._
import akka.actor.Props
import akka.contrib.throttle._
import akka.contrib.throttle.Throttler._
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import java.util.concurrent.TimeUnit._
import scala.concurrent.duration.Duration
import scala.concurrent._
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller {
  val externalWebService = Akka.system.actorOf(ExternalWebService.props)
  val throttler = Akka.system.actorOf( Props( classOf[TimerBasedThrottler], 1 msgsPer Duration( 3,  SECONDS )))
  throttler ! SetTarget(Some(externalWebService))


  def index( url: String ) = Action.async {
    implicit val timeout = Timeout(Duration( 300,  SECONDS ))
    val res = throttler ask WsRequest(GET, url)
    Logger.info( "url: " + url )

    res.map{ r =>
      r match {
        case r:Response => Ok(r.body).withHeaders(CONTENT_TYPE -> r.header("Content-type").getOrElse("text/plain"))
        case _ => Ok("Failed")
      }
    }.recover {
      case e:Exception => Ok("Failed:" + e.getMessage())
    }
  }

}
