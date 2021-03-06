package test.ldp

import akka.util.Timeout
import rww.ldp.auth.{Claim, WebIDVerifier, WACAuthZ, X509CertSigner}
import java.net.URL
import java.nio.file.{Path, Files}
import java.util.concurrent.TimeUnit
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import org.w3.banana._
import scala.concurrent.Future
import rww.ldp.LDPCommand._
import sun.security.x509.X500Name
import java.security.Principal
import rww.ldp._

import org.w3.banana.plantain.{LDPatch, Plantain}
import rww.ldp.actor.RWWActorSystem
import scala.Some
import scala.util.Try


class PlantainWebIDVerifierTest extends WebIDVerifierTest[Plantain](baseUri, dir)


/**
 * Test WebIDVerifier
 *
 */
abstract class WebIDVerifierTest[Rdf<:RDF](baseUri: Rdf#URI, dir: Path )
                                          (implicit val ops: RDFOps[Rdf],
                                           sparqlOps: SparqlOps[Rdf],
                                           sparqlGraph: SparqlGraph[Rdf],
                                           val recordBinder: binder.RecordBinder[Rdf],
                                           turtleWriter: RDFWriter[Rdf,Turtle],
                                           reader: RDFReader[Rdf, Turtle],
                                           patch: LDPatch[Rdf,Try])
  extends WordSpec with MustMatchers with BeforeAndAfterAll with TestHelper with TestGraphs[Rdf] {

  import ops._
  import syntax._

  implicit val timeout = Timeout(1, TimeUnit.MINUTES)
  val rww: RWWActorSystem[Rdf] = actor.RWWActorSystemImpl.plain[Rdf](baseUri, dir, testFetcher)

  implicit val authz =  new WACAuthZ[Rdf](new WebResource(rww))

  val webidVerifier = new WebIDVerifier(rww)

  val web = new WebResource[Rdf](rww)

  val bertailsCertSigner = new X509CertSigner(bertailsKeys.priv)
  val bertailsCert = bertailsCertSigner.generate(new X500Name("CN=Alex,O=W3C"),
    bertailsKeys.pub,2,new URL(bertails.toString))
  //of course bertails cannot create a cert with henry's public key, because not having the private key he would not
  //be able to connect to a server with it. So he must use a different key - his own will do for the test
  val bertailsFakeHenryCert = bertailsCertSigner.generate(new X500Name("CN=Henry, O=bblfish.net"),
    bertailsKeys.pub,3,new URL(henry.toString))

  "add bertails card and acls" in {
    val script = rww.execute(for {
      ldpc     <- createContainer(baseUri,Some("bertails"),Graph.empty)
      ldpcMeta <- getMeta(ldpc)
      card     <- createLDPR(ldpc,Some(bertailsCard.lastPathSegment),bertailsCardGraph)
      cardMeta <- getMeta(card)
      _        <- updateLDPR(ldpcMeta.acl.get, add=bertailsContainerAclGraph.toIterable)
      _        <- updateLDPR(cardMeta.acl.get, add=bertailsCardAclGraph.toIterable)
      rGraph   <- getLDPR(card)
      aclGraph <- getLDPR(cardMeta.acl.get)
      containerAclGraph <- getLDPR(ldpcMeta.acl.get)
    } yield {
      ldpc must be(bertailsContainer)
      cardMeta.acl.get must be(bertailsCardAcl)
      assert(rGraph isIsomorphicWith (bertailsCardGraph union containsRel).resolveAgainst(bertailsCard))
      assert(aclGraph isIsomorphicWith bertailsCardAclGraph.resolveAgainst(bertailsCardAcl))
      assert(containerAclGraph isIsomorphicWith bertailsContainerAclGraph.resolveAgainst(bertailsContainerAcl))
    })
    script.getOrFail()
  }

  "verify bertails' cert" in {
    val webidListFuture = webidVerifier.verify(Claim.ClaimMonad.point(bertailsCert))
    val futureWebids = Future.find(webidListFuture)(_=>true)
    val webidOpt = futureWebids.getOrFail()
    webidOpt.map{p=>p.getName} must be(Some(bertails.toString))
  }

  "verify bertails' fake cert fails" in {
    val webidListFuture = webidVerifier.verify(Claim.ClaimMonad.point(bertailsFakeHenryCert))
    val firstFuture: Future[Option[Principal]] = Future.find(webidListFuture)(_=>true)
    val res = firstFuture.getOrFail()
    res must be(None)
  }


  val henryCertSigner = new X509CertSigner(henryKeys.priv)
  val henryCert = henryCertSigner.generate(new X500Name("CN=Henry, O=bblfish.net"),henryKeys.pub,2,new URL(henry.toString))
  val henrysFakeBertailsCert = henryCertSigner.generate(new X500Name("CN=Alex,O=W3C"),henryKeys.pub,3,new URL(bertails.toString))


  "verify Henry's cert" in {
    val webidListFuture = webidVerifier.verify(Claim.ClaimMonad.point(henryCert))
    val futureWebids = Future.find(webidListFuture)(_=>true)
    val webids = futureWebids.getOrFail()
    webids.map{p=>p.getName} must be(Some(henry.toString))
  }

  "verify Henry's fake cert fails" in {
    val webidListFuture = webidVerifier.verify(Claim.ClaimMonad.point(henrysFakeBertailsCert))
    val futureWebids = Future.find(webidListFuture)(_=>true)
    val res = futureWebids.getOrFail()
    res must be(None)
  }


}
