package actors
import akka.actor.Actor
import akka.actor.Props
import akka.pattern.{ask, pipe}
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits._
class ExternalWebService extends Actor {
  def receive = {
    case WsRequest(GET, url) => WS.url( url ).get pipeTo sender
  }
}

object ExternalWebService {
  def props: Props = Props(classOf[ExternalWebService])
}

case class WsRequest( method: Method, url: String )
sealed trait Method
case object GET extends Method

