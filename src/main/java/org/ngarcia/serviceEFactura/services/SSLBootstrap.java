package org.ngarcia.serviceEFactura.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import javax.net.ssl.SSLContext;

@Startup
@Singleton
@Priority(1)
public class SSLBootstrap {
   @PostConstruct
   public void initDefaultSSLContext() {
      try {
         SSLContext sc = SSLContext.getInstance("TLS"); //TLSv1.2
         sc.init(null, null, null);
         SSLContext.setDefault(sc);
         System.out.println("✅ Default SSLContext inyectado: " + sc.getProtocol());
      } catch (Exception e) {
         throw new RuntimeException("No pude inicializar DefaultSSLContext", e);
      }
   }
}

