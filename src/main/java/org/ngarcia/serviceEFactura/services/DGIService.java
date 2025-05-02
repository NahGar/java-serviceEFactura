package org.ngarcia.serviceEFactura.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.ngarcia.serviceEFactura.utils.XMLSigner;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

@ApplicationScoped
public class DGIService {
    private static final String KEYSTORE_PATH = "certprueba-1234.pfx";
    private static final String KEYSTORE_PASS = "1234";

    public String signAndSendToDGI(String unsignedXml) throws Exception {

        System.out.println("Java version: " + System.getProperty("java.version"));

        for (Provider provider : Security.getProviders()) {
            System.out.println(provider.getName());
        }

        //System.setProperty("javax.net.ssl.keyStore", "D:/Desarrollo/Personal/Java/serviceEFactura/src/main/resources/certprueba-1234.pfx");
        System.setProperty("javax.net.ssl.keyStore", "D:/Java/Proyectos/servicioEFactura/src/main/resources/certprueba-1234.pfx");
        System.setProperty("javax.net.ssl.keyStorePassword", "1234");
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");

        System.setProperty("javax.net.ssl.trustStore", "D:/Java/Proyectos/servicioEFactura/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");
        //System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
        System.setProperty("javax.net.debug", "ssl,handshake");

        // Forzar TLS 1.2 (requerido por muchos servidores modernos)
        System.setProperty("https.protocols", "TLSv1.2");
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

        // Carga el certificado como InputStream desde el classpath
        InputStream keystoreStream = getClass().getClassLoader().getResourceAsStream(KEYSTORE_PATH);

        if (keystoreStream == null) {
            throw new FileNotFoundException("Certificado no encontrado en classpath: " + KEYSTORE_PATH);
        }

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(keystoreStream, KEYSTORE_PASS.toCharArray());

        String signedXML = XMLSigner.signXML(unsignedXml, ks, KEYSTORE_PASS);
        System.out.println("SIGNED:"+signedXML);

        // Cierra el stream (opcional pero recomendado)
        keystoreStream.close();

        return ClienteDGIService.enviarCFE(signedXML); // Tu cliente SOAP existente
    }
}
