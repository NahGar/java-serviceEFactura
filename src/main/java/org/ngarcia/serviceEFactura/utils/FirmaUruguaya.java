package org.ngarcia.serviceEFactura.utils;

import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class FirmaUruguaya {
   static {
      org.apache.xml.security.Init.init();
   }

   public Document firmarCFE(Document docCFE, PrivateKey clavePrivada,
                             X509Certificate certificado) throws Exception {

      XMLSignature firma = new XMLSignature(docCFE, "",
              XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1); // RSA-SHA1 según DGI

      Element root = docCFE.getDocumentElement();
      root.appendChild(firma.getElement());

      // Transformaciones requeridas
      Transforms transforms = new Transforms(docCFE);
      transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
      transforms.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS);

      // Añadir referencia al documento completo (excluyendo adenda)
      firma.addDocument("", transforms, Constants.ALGO_ID_DIGEST_SHA1);

      // Info del certificado (PKI Uruguay)
      firma.addKeyInfo(certificado);
      firma.addKeyInfo(certificado.getPublicKey());

      // Firmar
      firma.sign(clavePrivada);

      return docCFE;
   }
}
