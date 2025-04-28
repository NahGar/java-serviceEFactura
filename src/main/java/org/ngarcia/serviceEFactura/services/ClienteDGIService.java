package org.ngarcia.serviceEFactura.services;

import uy.gub.dgi.cfe.*;
import javax.xml.ws.BindingProvider;

public class ClienteDGIService {

   public static String enviarCFE(String signedXML) {
      try {
         // Configuración SSL (ya no es static)
         System.setProperty("javax.net.ssl.keyStore", "src/main/resources/certprueba-1234.pfx");
         System.setProperty("javax.net.ssl.keyStorePassword", "1234");

         WSEFactura servicio = new WSEFactura();
         WSEFacturaSoapPort port = servicio.getWSEFacturaSoapPort();

         BindingProvider bp = (BindingProvider) port;
         bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                 "https://efactura.dgi.gub.uy:6443/ePrueba/ws_eprueba");

         // Crear request
         ObjectFactory factory = new ObjectFactory();
         WSEFacturaEFACRECEPCIONSOBRE request = factory.createWSEFacturaEFACRECEPCIONSOBRE();
         Data data = factory.createData();
         data.setXmlData(signedXML);
         request.setDatain(data);

         // Invocar servicio
         WSEFacturaEFACRECEPCIONSOBREResponse response = port.efacrecepcionsobre(request);
         return response.getDataout().getXmlData();
      } catch (Exception e) {
         throw new RuntimeException("Error al enviar CFE a la DGI: " + e.getMessage(), e);
      }
   }
}