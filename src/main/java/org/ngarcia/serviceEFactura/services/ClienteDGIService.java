package org.ngarcia.serviceEFactura.services;

import uy.gub.dgi.cfe.*;
import javax.xml.ws.BindingProvider;

public class ClienteDGIService {

   private static final String ENDPOINT = "https://efactura.dgi.gub.uy:6443/ePrueba/ws_eprueba";
   private static final String KEYSTORE_PATH = "src/test/resources/certprueba(1234).pfx";
   private static final String KEYSTORE_PASS = "1234";

   public static void main(String[] args) {
      // Configuración SSL
      System.setProperty("javax.net.ssl.keyStore", KEYSTORE_PATH);
      System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASS);

      // 1. Crear instancia del servicio
      WSEFactura servicio = new WSEFactura();

      // 2. Obtener el puerto SOAP
      WSEFacturaSoapPort port = servicio.getWSEFacturaSoapPort();

      // 3. Configurar endpoint
      BindingProvider bp = (BindingProvider) port;
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT);

      // 4. Ejemplo: Enviar un CFE
      enviarCFE(port);
   }

   public static void enviarCFE(WSEFacturaSoapPort port) {
      ObjectFactory factory = new ObjectFactory();

      // Crear request
      WSEFacturaEFACRECEPCIONSOBRE request = factory.createWSEFacturaEFACRECEPCIONSOBRE();
      Data data = factory.createData();
      data.setXmlData("<CFE xmlns=\"http://dgi.gub.uy\">...</CFE>"); // XML completo del CFE
      request.setDatain(data);

      // Invocar servicio
      WSEFacturaEFACRECEPCIONSOBREResponse response = port.efacrecepcionsobre(request);

      // Procesar respuesta
      System.out.println("Respuesta DGI: " + response.getDataout().getXmlData());
   }
}