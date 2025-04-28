package org.ngarcia.serviceEFactura.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.ngarcia.serviceEFactura.utils.XMLSigner;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;

@ApplicationScoped
public class DGIService {
    private static final String KEYSTORE_PATH = "certprueba-1234.pfx";
    private static final String KEYSTORE_PASS = "1234";

    public String signAndSendToDGI(String unsignedXml) throws Exception {

        // Carga el certificado como InputStream desde el classpath
        InputStream keystoreStream = getClass().getClassLoader().getResourceAsStream(KEYSTORE_PATH);

        if (keystoreStream == null) {
            throw new FileNotFoundException("Certificado no encontrado en classpath: " + KEYSTORE_PATH);
        }

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(keystoreStream, KEYSTORE_PASS.toCharArray());

        String signedXML = XMLSigner.signXML(unsignedXml, keystoreStream, KEYSTORE_PASS);

        // Cierra el stream (opcional pero recomendado)
        //keystoreStream.close();

        return ClienteDGIService.enviarCFE(signedXML); // Tu cliente SOAP existente
    }
}
