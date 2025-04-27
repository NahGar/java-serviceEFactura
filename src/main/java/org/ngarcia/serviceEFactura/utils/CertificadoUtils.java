package org.ngarcia.serviceEFactura.utils;

import java.security.KeyStore;

public class CertificadoUtils {

   public static KeyStore cargarCertificado(String ruta, String password) throws Exception {
      KeyStore ks = KeyStore.getInstance("PKCS12");
      ks.load(
              Thread.currentThread().getContextClassLoader().getResourceAsStream(ruta),
              password.toCharArray()
      );
      return ks;
   }
}
