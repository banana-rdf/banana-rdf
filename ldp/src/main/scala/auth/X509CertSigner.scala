/*
 * Copyright (c) 2011 Henry Story (bblfish.net)
 * under the MIT licence defined
 *    http://www.opensource.org/licenses/mit-license.html
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.w3.banana.ldp.auth;

import java.math.BigInteger
import java.net.URL
import java.security._
import java.security.cert.X509Certificate
import java.util.Date
import sun.security.x509._
import java.security.interfaces.RSAPublicKey
import javax.security.auth.x500.X500Principal


object X509CertSigner  {
  val WebID_DN = new X500Principal("""CN=WebID, O=∅""")


  //  def apply( keyStoreLoc: Option[URL],
//             keyStoreType: Option[String],
//             password: Option[String],
//             alias: Option[String]): Option[X509CertSigner] = {
//    try {
//      for {
//        loc <- keyStoreLoc
//        tp <- keyStoreType
//      } yield {
//        val pass = password.map(_.toCharArray).getOrElse(null)
//        val alias2 = alias.getOrElse("")  //todo there are better ways of finding an alias than this
//        val ks = KeyStore.getInstance(tp)
//        IO.use(loc.openStream()) {
//          in => ks.load(in, pass)
//        }
//        val privateKey = ks.getKey(alias2, pass).asInstanceOf[PrivateKey]
//        val certificate = ks.getCertificate(alias2).asInstanceOf[X509Certificate]
//        //one could verify that indeed this is the private key corresponding to the public key in the cert.
//        new X509CertSigner(certificate, privateKey)
//      }
//    } catch {
//      case e: Exception => {
//        logger.warn("could not load TLS certificate for certificate signing service", e)
//        None
//      }
//    }
//  }

//  def apply( keyStoreLoc: URL,
//             keyStoreType: String,
//             password: String,
//             alias: String): X509CertSigner =
//    apply(Option(keyStoreLoc),Option(keyStoreType),Option(password),Option(alias)).get

}

class X509CertSigner(
        val signatorPrivateKey: PrivateKey,
        signatorPrincipal: java.security.Principal=X509CertSigner.WebID_DN) {

  val sigAlg = signatorPrivateKey.getAlgorithm match {
    case "RSA" =>  "SHA1withRSA"
    case "DSA" =>  "SHA1withDSA"
    //else will throw a case exception
  }


  /**
   * Adapted from http://bfo.com/blog/2011/03/08/odds_and_ends_creating_a_new_x_509_certificate.html
   * The libraries used here are sun gpled code. This is much lighter to use than bouncycastle. All VMs that already
   * have these classes don't need to download the code. It should be easy in scala to create a build that can decide
   * if these need to be added to the classpath. I think the code just looks better than bouncycastle too.
   *
   * WARNING THIS IS   in construction
   *
   * Look in detail at http://www.ietf.org/rfc/rfc2459.txt
   *
   * Create a self-signed X.509 Certificate
   * @param subjectDN the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
   * @param subjectKey the public key for the subject
   * @param days how many days from now the Certificate is valid for
   * @param webId a WebID to place in the Subject Alternative Name field of the Cert to be generated
   */
  def generate(
      subjectDN: X500Name,
      subjectKey: RSAPublicKey,
      days: Int,
      webId: URL): X509Certificate = {   //todo: the algorithm should be deduced from private key in part

    var info = new X509CertInfo
    val from = new Date(System.currentTimeMillis()-10*1000*60) //start 10 minutes ago, to avoid network trouble
    val to = new Date(from.getTime + days*24*60*60*1000) 
    val interval = new CertificateValidity(from, to)
    val serialNumber = new BigInteger(64, new SecureRandom)
    val issuerXN = new X500Name(signatorPrincipal.toString)

    info.set(X509CertInfo.VALIDITY, interval)
    info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(serialNumber))
    info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(subjectDN))
    info.set(X509CertInfo.ISSUER, new CertificateIssuerName(issuerXN))
    info.set(X509CertInfo.KEY, new CertificateX509Key(subjectKey))
    info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3))

    //
    //extensions
    //
    val extensions = new CertificateExtensions

    val san =
      new SubjectAlternativeNameExtension(
          true,
          new GeneralNames().add(
              new GeneralName(new URIName(webId.toExternalForm))))
    
    extensions.set(san.getName, san)

    val basicCstrExt = new BasicConstraintsExtension(false,1)
    extensions.set(basicCstrExt.getName,basicCstrExt)

    {
      import KeyUsageExtension._
      val keyUsage = new KeyUsageExtension
      val usages =
        List(DIGITAL_SIGNATURE, NON_REPUDIATION, KEY_ENCIPHERMENT, KEY_AGREEMENT)
      usages foreach { usage => keyUsage.set(usage, true) }
      extensions.set(keyUsage.getName,keyUsage)
    }

    {
      import NetscapeCertTypeExtension._
      val netscapeExt = new NetscapeCertTypeExtension
      List(SSL_CLIENT, S_MIME) foreach { ext => netscapeExt.set(ext, true) }
      extensions.set(
        netscapeExt.getName,
        new NetscapeCertTypeExtension(false, netscapeExt.getExtensionValue().clone))
    }
      
    val subjectKeyExt =
      new SubjectKeyIdentifierExtension(new KeyIdentifier(subjectKey).getIdentifier)

    extensions.set(subjectKeyExt.getName, subjectKeyExt)
    
    info.set(X509CertInfo.EXTENSIONS, extensions)

    val algo = signatorPrivateKey.getAlgorithm match {
      case "DSA" => new AlgorithmId(AlgorithmId.sha1WithDSA_oid )
      case "RSA" => new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid)
      case _ => sys.error("Don't know how to sign with this type of key")  
    }

    info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo))

    // Sign the cert to identify the algorithm that's used.
    val tmpCert = new X509CertImpl(info)
    tmpCert.sign(signatorPrivateKey, algo.getName)

    //update the algorithm and re-sign
    val sigAlgo = tmpCert.get(X509CertImpl.SIG_ALG).asInstanceOf[AlgorithmId]
    info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, sigAlgo)
    val cert = new X509CertImpl(info)
    cert.sign(signatorPrivateKey,algo.getName)
    return cert
  }

  val clonesig : Signature =  sig

  def sig: Signature = {
    if (clonesig != null && clonesig.isInstanceOf[Cloneable]) clonesig.clone().asInstanceOf[Signature]
    else {
      val signature = Signature.getInstance(sigAlg)
      signature.initSign(signatorPrivateKey)
      signature
    }
  }

  def sign(string: String): Array[Byte] = {
      val signature = sig
      signature.update(string.getBytes("UTF-8"))
      signature.sign
  }

}

