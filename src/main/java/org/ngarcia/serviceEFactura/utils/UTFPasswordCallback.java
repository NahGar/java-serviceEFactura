package org.ngarcia.serviceEFactura.utils;

import org.apache.wss4j.common.ext.WSPasswordCallback;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

public class UTFPasswordCallback implements CallbackHandler {
   public void handle(Callback[] callbacks) {
      for (Callback callback : callbacks) {
         WSPasswordCallback pc = (WSPasswordCallback) callback;
         if ("mycertalias".equals(pc.getIdentifier())) {
            pc.setPassword("password"); // Contraseña del keystore
            break;
         }
      }
   }
}
