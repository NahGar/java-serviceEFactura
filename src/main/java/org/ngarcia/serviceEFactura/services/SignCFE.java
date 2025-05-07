package org.ngarcia.serviceEFactura.services;

import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.UUID;

public class SignCFE {

   public static void sign(Element cfeElement, KeyStore ks, X509Certificate cert) throws Exception {
      // Configurar atributo ID
      String id = "CFE-" + UUID.randomUUID().toString();
      cfeElement.setAttributeNS(null, "Id", id);

      // Obtener clave privada
      String alias = ks.aliases().nextElement();
      String keyPassword = "1234";
      PrivateKey privateKey = (PrivateKey) ks.getKey(alias, keyPassword.toCharArray());

      // Crear contexto de firma
      XMLSignature sig = new XMLSignature(cfeElement.getOwnerDocument(), "",
              XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256,
              "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");

      // Configurar la referencia
      Transforms transforms = new Transforms(cfeElement.getOwnerDocument());
      transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
      transforms.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS);
      sig.addDocument("#" + id, transforms, Constants.ALGO_ID_DIGEST_SHA256);

      // Añadir KeyInfo CORREGIDO
      KeyInfo keyInfo = new KeyInfo(cfeElement.getOwnerDocument());
      keyInfo.addKeyValue(cert.getPublicKey());

      // Añadir certificado X509 CORREGIDO
      org.apache.xml.security.keys.content.X509Data x509Data =
              new org.apache.xml.security.keys.content.X509Data(cfeElement.getOwnerDocument());
      x509Data.addCertificate(cert);
      keyInfo.add(x509Data);

      // Vincular KeyInfo con la firma
      sig.getSignatureElement().appendChild(keyInfo.getElement());

      // Insertar firma
      Node firstChild = cfeElement.getFirstChild();
      if (firstChild != null) {
         cfeElement.insertBefore(sig.getElement(), firstChild);
      } else {
         cfeElement.appendChild(sig.getElement());
      }

      // Firmar
      sig.sign(privateKey);
   }
}