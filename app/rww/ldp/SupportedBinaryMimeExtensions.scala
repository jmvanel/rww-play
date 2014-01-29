package rww.ldp

import org.w3.banana._

/**
 * @author Sebastien Lorber (lorber.sebastien@gmail.com)
 */
// TODO this mapping should be configurable through configuration and not hardcoded maybe
object SupportedBinaryMimeExtensions extends MimeExtensions {

  object ApplicationPdf extends MimeType("application/pdf")
  object ApplicationJs extends MimeType("application/javascript")
  // TODO add other supported mime types for binary files like Excel and Word files etc...



  val mimeExt = collection.immutable.Map(
    ImageJpegMime -> ".jpg",
    ImageGifMime -> ".gif",
    ImagePngMime -> ".png",
    TextHtmlMime -> ".html",
    ApplicationJs -> ".js",
    ApplicationPdf -> ".pdf"
  )

  val extMime = {
    val content = for {
      (k,v) <- mimeExt.toSeq
    } yield (v,k)
    collection.immutable.Map(content: _*)
  }


  // /!\ in the banana rdf subclass the extension must be provided with the dot so we keep this behavior here
  def extension(mime: MimeType) = mimeExt.get(mime)
  def mime(extension: String)   = extMime.get(extension)

}
