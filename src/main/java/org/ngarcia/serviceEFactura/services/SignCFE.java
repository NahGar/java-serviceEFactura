package org.ngarcia.serviceEFactura.services;

import org.apache.xml.security.Init;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.UUID;

public class SignCFE {

   // URI oficial de WSU-Utility
   private static final String WSU_NS =
           "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

   static {
      Init.init();
      // Registrar el namespace ds automáticamente
       try {
           ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "ds");
       } catch (XMLSecurityException e) {
           e.printStackTrace();
       }
   }

   public static void sign(Element cfeElement, KeyStore ks, X509Certificate cert, String alias) {

      try {
         // 1) Generar un Id y asignarlo en el namespace WSU
         //String id = "CFE-" + UUID.randomUUID();
         // declarar namespace wsu en el propio elemento CFE
         //cfeElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:wsu", WSU_NS);
         // poner el atributo wsu:Id
         //cfeElement.setAttributeNS(WSU_NS, "wsu:Id", id);

         // 2) Obtener clave privada
         PrivateKey privateKey = (PrivateKey) ks.getKey(alias, "1234".toCharArray());

         // 3) Crear el objeto XMLSignature usando SHA256 o SHA1 según tu esquema
         //    Si quieres SHA1 (como el ejemplo), usa ALGO_ID_SIGNATURE_RSA_SHA1
         String signatureAlgo = XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;
         // String signatureAlgo = XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1;
         XMLSignature sig = new XMLSignature(
                 cfeElement.getOwnerDocument(),
                 "",
                 signatureAlgo
         );

         // 4) Transforms: primero enveloped, luego c14n (con o sin comentarios)
         Transforms transforms = new Transforms(cfeElement.getOwnerDocument());
         transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
         transforms.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS);

         // 5) Añadir la referencia con URI vacío
         sig.addDocument("", transforms, "http://www.w3.org/2001/04/xmlenc#sha256");

         // 6) Construir KeyInfo con certificado
         KeyInfo ki = sig.getKeyInfo();
         if (ki == null) {
            ki = new KeyInfo(cfeElement.getOwnerDocument());
         }
         // añadir clave pública
         ki.addKeyValue(cert.getPublicKey());
         // añadir datos X509
         org.apache.xml.security.keys.content.X509Data x509Data =
                 new org.apache.xml.security.keys.content.X509Data(cfeElement.getOwnerDocument());
         x509Data.addCertificate(cert);
         ki.add(x509Data);

         // 7) Insertar el elemento <ds:Signature> como primer hijo de <CFE>
         Node firstChild = cfeElement.getFirstChild();
         if (firstChild != null) {
            cfeElement.insertBefore(sig.getElement(), firstChild);
         } else {
            cfeElement.appendChild(sig.getElement());
         }

         // 8) Firmar
         sig.sign(privateKey);

         // --- depuración: imprimir la firma en consola ---
         //TransformerFactory tf = TransformerFactory.newInstance();
         //Transformer transformer = tf.newTransformer();
         //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         //StringWriter sw = new StringWriter();
         //transformer.transform(new DOMSource(sig.getElement()), new StreamResult(sw));
         //System.out.println(">>> ELEMENTO Signature:\n" + sw.toString());

      } catch (Exception e) {
         System.out.println("Error en SingCFE: " + e.getMessage());;
      }
   }
}