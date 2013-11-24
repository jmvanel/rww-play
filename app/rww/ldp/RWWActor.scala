package rww.ldp

import org.w3.banana.{syntax, RDFOps, RDF}
import akka.util.Timeout
import akka.actor.ActorRef
import scalaz.{\/-, -\/}
import java.net.{URI=>jURI}

object RWWebActor {
  def local(u: jURI, base: jURI): Option[String] = {
    val res = if ((!u.isAbsolute ) || (u.getScheme == base.getScheme && u.getHost == base.getHost && u.getPort == base.getPort)) {
      if (u.getPath.startsWith(base.getPath)) {
        val res = u.getPath.substring(base.getPath.size)
        val sections = res.split('/')
        val fileName = sections.last
        var idot= fileName.indexOf('.')
        val treated = if (idot>0) {
          sections.update(sections.length-1,fileName.substring(0,idot))
          sections.toSeq
        }else if (idot==0) sections.toSeq.dropRight(1)
        else sections.toSeq
        Option(treated.mkString("/"))
      } else None
    } else None
    res
  }

}

/**
 *
 * A actor that receives commands and ships them off either to a WebActor or to an LDPSActor
 *
 * @param baseUri
 * @param ops
 * @param timeout
 * @tparam Rdf
 */
class RWWebActor[Rdf<:RDF](val baseUri: Rdf#URI)
                          (implicit ops: RDFOps[Rdf], timeout: Timeout) extends RActor {
  import syntax.URISyntax.uriW
  import RWWebActor._

  var rootContainer: Option[ActorRef] = None
  var web : Option[ActorRef] = None


  def receive = returnErrors {
    case Scrpt(script) => {
      script.resume match {
        case command: -\/[LDPCommand[Rdf, LDPCommand.Script[Rdf,_]]] => forwardSwitch(Cmd(command.a))
        case \/-(res) => sender ! res
      }
    }
    case cmd: Cmd[Rdf,_] => forwardSwitch(cmd)
    case WebActor(webActor) => {
      log.info(s"setting web actor to <$webActor> ")
      web = Some(webActor)
    }
    case LDPSActor(ldps) => {
      log.info(s"setting rootContainer to <$ldps> ")
      rootContainer = Some(ldps)
    }
  }

  /** We in fact ignore the R and A types, since we cannot capture */
  protected def forwardSwitch[A](cmd: Cmd[Rdf,A]) {
    local(cmd.command.uri.underlying,baseUri.underlying).map { path=>
      rootContainer match {
        case Some(root) => {
          val p = root.path / path.split('/').toIterable
          val to = context.actorSelection(p)
          log.info(s"forwarding message $cmd to akka('$path')=$to ")
          to.tell(cmd,context.sender)
        }
        case None => log.warning("RWWebActor not set up yet: missing rootContainer")
      }
    } getOrElse {
      //todo: this relative uri comparison is too simple.
      //     really one should look to see if it
      //     is the same host and then send it to the local lpdserver ( because a remote server may
      //     link to this server ) and if so there is no need to go though the external http layer to
      //     fetch graphs
      web.map {
        log.info(s"sending message $cmd to general web agent <$web>")
        _ forward cmd
      }.getOrElse(log.warning("RWWebActor not set up yet: missing web actor"))
    }

  }


}