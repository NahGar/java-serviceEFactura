package org.ngarcia.serviceEFactura.services;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.Handler;
import org.ngarcia.serviceEFactura.SOAP.LoggingSOAPHandler;
import org.ngarcia.serviceEFactura.SOAP.WSSecurityHeaderSOAPHandler;
import uy.gub.dgi.cfe.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class ClienteDGIService {

   public static String enviarCFE(String signedXML, KeyStore ks, String alias, X509Certificate cert) {
      try {
         // Crear el servicio y el puerto
         WSEFactura servicio = new WSEFactura();
         WSEFacturaSoapPort port = servicio.getWSEFacturaSoapPort();

         // Configurar la URL del servicio web
         BindingProvider bp = (BindingProvider) port;
         bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                 "https://efactura.dgi.gub.uy:6443/ePrueba/ws_eprueba");
         bp.getRequestContext().put("SIGNED_XML", signedXML);

         // Crear el objeto de solicitud
         ObjectFactory factory = new ObjectFactory();
         WSEFacturaEFACRECEPCIONSOBRE request = factory.createWSEFacturaEFACRECEPCIONSOBRE();
         Data data = factory.createData();
         data.setXmlData(signedXML);
         request.setDatain(data);

         // Crear la cadena de manejadores y agregar el WS-Security Header Handler
         List<Handler> handlerChain = new ArrayList<>();

         PrivateKey privateKey = (PrivateKey) ks.getKey(alias, "1234".toCharArray());
         handlerChain.add(new WSSecurityHeaderSOAPHandler(cert, privateKey));

         //no mover logging porque deja loguear
         handlerChain.add(new LoggingSOAPHandler());

         // Establecer la cadena de manejadores en el puerto
         ((BindingProvider) port).getBinding().setHandlerChain(handlerChain);

         // Invocar el servicio web
         WSEFacturaEFACRECEPCIONSOBREResponse response = port.efacrecepcionsobre(request);

         // Procesar la respuesta
         //System.out.println("RESPONSE " + response.getDataout().getXmlData());
         return response.getDataout().getXmlData();

      } catch (Exception e) {
         System.out.println("ERROR:" + e.getMessage());
         e.printStackTrace();
         throw new RuntimeException("Error al enviar CFE a la DGI: " + e.getMessage(), e);
      }
   }
}
