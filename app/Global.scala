/**
 * @author ouertani@gmail.com
 * Date: 24/11/2013
 */

import controllers.ldp
import controllers.ldp.ReadWriteWebController
import play.api._
import mvc.RequestHeader

object Global extends GlobalSettings {

  override def onRouteRequest(req: RequestHeader) = {
    import rww.play.EnhancedRequestHeader

    val uri = req.getAbsoluteURI
    if (
      uri.getPath.startsWith("/assets/")
      || uri.getPath.startsWith("/srv/")
      || uri.getHost().startsWith("www") ) {
      super.onRouteRequest(req)
    }
    else if (uri.getHost != controllers.plantain.host) {
      req.method match {
        case "GET" => Some(ReadWriteWebController.get(req.path))
        case "POST" => Some(ldp.ReadWriteWebController.post(req.path))
        case "PATCH" => Some(ldp.ReadWriteWebController.patch(req.path))
        case "MKCOL" => Some(ldp.ReadWriteWebController.mkcol(req.path))
        case "HEAD" => Some(ldp.ReadWriteWebController.head(req.path))
        case "OPTIONS" => Some(ReadWriteWebController.options(req.path))
        case "PUT" =>  Some(ldp.ReadWriteWebController.put(req.path))
        case "DELETE" => Some(ldp.ReadWriteWebController.delete(req.path))
      }
    }
    else {
      super.onRouteRequest(req)
    }
  }

}
