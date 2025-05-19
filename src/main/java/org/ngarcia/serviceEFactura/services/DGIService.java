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

        String rutReceptor  = "219999830019";
        String rucEmisor    = "219999820013";
        String idEmisor     = "3009";
        int    cantCFE      = 1;
        String fecha        = "2023-10-01T13:10:00-03:00";
        byte[] der = cert.getEncoded();
        String certB64      = Base64.getEncoder().encodeToString(der);

        // 3) Crea el wrapper EnvióCFE
        Element envio = doc.createElementNS(
                cfeNS, "DGICFE:EnvioCFE"
        );
        envio.setAttribute("version", "1.0");
        envio.setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                "http://cfe.dgi.gub.uy EnvioCFE_v1.11.xsd"
        );
        envio.setAttribute("xmlns:DGICFE", cfeNS);
        envio.setAttribute("xmlns:xsi",
                "http://www.w3.org/2001/XMLSchema-instance"
        );

        // 4) Construye la Carátula dentro de 'envio'
        Element car = doc.createElementNS(cfeNS, "DGICFE:Caratula");
        car.setAttribute("version", "1.0");
        envio.appendChild(car);

        Element rutReceptorEl = doc.createElementNS(cfeNS, "DGICFE:RutReceptor");
        rutReceptorEl.setTextContent("219999830019");
        car.appendChild(rutReceptorEl);

        Element rucEmisorEl = doc.createElementNS(cfeNS, "DGICFE:RUCEmisor");
        rucEmisorEl.setTextContent("219999820013");
        car.appendChild(rucEmisorEl);

        Element idEmisorEl = doc.createElementNS(cfeNS, "DGICFE:Idemisor");
        idEmisorEl.setTextContent("3009");
        car.appendChild(idEmisorEl);

        Element cantCFEEl = doc.createElementNS(cfeNS, "DGICFE:CantCFE");
        cantCFEEl.setTextContent("1");
        car.appendChild(cantCFEEl);

        Element fechaEl = doc.createElementNS(cfeNS, "DGICFE:Fecha");
        fechaEl.setTextContent("2023-10-01T13:10:00-03:00");
        car.appendChild(fechaEl);

        Element certEl = doc.createElementNS(cfeNS, "DGICFE:X509Certificate");
        certEl.setTextContent(certB64);  // Aquí va tu certificado codificado en Base64
        car.appendChild(certEl);
        /*
        for (Map.Entry<String,String> e : Map.of(
                "DGICFE:RutReceptor",      rutReceptor,
                "DGICFE:RUCEmisor",        rucEmisor,
                "DGICFE:Idemisor",         idEmisor,
                "DGICFE:CantCFE",          String.valueOf(cantCFE),
                "DGICFE:Fecha",            fecha,
                "DGICFE:X509Certificate",  certB64
        ).entrySet()) {
            Element el = doc.createElementNS(cfeNS, e.getKey());
            el.setTextContent(e.getValue());
            car.appendChild(el);
        }*/

        // 5) Mueve el <CFE> bajo tu wrapper
        //Node parent = cfe.getParentNode();
        //parent.replaceChild(envio, cfe);
        //envio.appendChild(cfe);

        Node parent = cfe.getParentNode();
        parent.replaceChild(envio, cfe);
        envio.appendChild(cfe);

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

