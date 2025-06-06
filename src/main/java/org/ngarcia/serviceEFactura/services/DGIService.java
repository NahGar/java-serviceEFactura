package org.ngarcia.serviceEFactura.services;

import java.io.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import jakarta.enterprise.context.ApplicationScoped;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;

@ApplicationScoped
public class DGIService {
    private static final String KEYSTORE_NAME = "certprueba-1234.pfx";
    private static final String KEYSTORE_PASS = "1234";
    private static final String KEYSTORE_PATH = "D:\\Java\\Proyectos\\servicioEFactura\\src\\main\\resources\\certprueba-1234.pfx";
    private static final String TRUSTSTORE_PATH = "D:\\Java\\Proyectos\\servicioEFactura\\src\\main\\resources\\truststore.jks";
    private static final String TRUSTSTORE_PASS = "123456";

    static {
        org.apache.xml.security.Init.init();
    }

    public String signAndSendToDGI(String unsignedXml) throws Exception {
        // Configuración SSL y TLS (mantener tu configuración actual)
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_PATH);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASS);
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
        System.setProperty("https.protocols", "TLSv1.2");

        System.setProperty("javax.net.ssl.trustStore",TRUSTSTORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword",TRUSTSTORE_PASS);
        System.setProperty("javax.net.ssl.trustStoreType","JKS");

        // Cargar KeyStore
        InputStream keystoreStream = getClass().getClassLoader().getResourceAsStream(KEYSTORE_NAME);
        if (keystoreStream == null) {
            throw new FileNotFoundException("Certificado no encontrado: " + KEYSTORE_NAME);
        }
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(keystoreStream, KEYSTORE_PASS.toCharArray());
        keystoreStream.close();

        String alias = "";
        Enumeration<String> aliases = ks.aliases();
        if (aliases.hasMoreElements()) {
            alias = aliases.nextElement();
        }

        // Obtener el certificado X.509 del KeyStore
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

        // Parsear XML
        Document doc = parseXml(unsignedXml);

        // Firmar cada elemento <CFE>
        //String cfeNS = "http://cfe.dgi.gub.uy";
        //NodeList cfeNodes = doc.getElementsByTagNameNS(cfeNS, "CFE");
        //for (int i = 0; i < cfeNodes.getLength(); i++) {
        //    Element cfe = (Element) cfeNodes.item(i);
        //    SignCFE.sign(cfe, ks, cert, alias);
        //}

        String cfeNS = "http://cfe.dgi.gub.uy";
        NodeList cfeNodes = doc.getElementsByTagNameNS(cfeNS, "CFE");
        if (cfeNodes.getLength() != 1) {
            throw new IllegalStateException("Se esperaba exactamente un <CFE>");
        }
        Element cfe = (Element) cfeNodes.item(0);
        SignCFE.sign(cfe, ks, cert, alias);

        doc = EnvioGenerator.build(doc, cfe, cfeNS, cert);

        // Convertir Document a String
        String signedXml = documentToString(doc);
        System.out.println("DOC firmado:"+signedXml);

        // Enviar el XML firmado
        return ClienteDGIService.enviarCFE(signedXml, ks, alias, cert);
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private String documentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
}

