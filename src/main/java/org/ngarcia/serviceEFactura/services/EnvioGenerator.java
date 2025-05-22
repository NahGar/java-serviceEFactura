package org.ngarcia.serviceEFactura.services;

import org.ngarcia.serviceEFactura.utils.Varios;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

public class EnvioGenerator {

    public static Document build(Document doc, Element cfe, String cfeNS, X509Certificate cert) throws CertificateEncodingException {

        String rutReceptor  = "219999830019";
        String rucEmisor    = Optional.ofNullable(cfe.getElementsByTagNameNS(cfeNS, "RUCEmisor"))
                .filter(nodes -> nodes.getLength() > 0)
                .map(nodes -> nodes.item(0).getTextContent())
                .orElse("");
        String idEmisor     = "3009";
        String cantCFE      = "1";
        String fecha        = Varios.formatearFecha(LocalDateTime.now());
        byte[] der = cert.getEncoded();
        String certB64      = Base64.getEncoder().encodeToString(der);

        // 1) Crea el wrapper EnvioCFE
        Element envio = doc.createElementNS(cfeNS, "DGICFE:EnvioCFE");
        envio.setAttribute("version", "1.0");
        envio.setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                "http://cfe.dgi.gub.uy EnvioCFE_v1.11.xsd"
        );
        envio.setAttribute("xmlns:DGICFE", cfeNS);
        envio.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");

        // 2) Construye la Carátula dentro de 'envio'
        Element car = doc.createElementNS(cfeNS, "DGICFE:Caratula");
        car.setAttribute("version", "1.0");
        envio.appendChild(car);

        Element rutReceptorEl = doc.createElementNS(cfeNS, "DGICFE:RutReceptor");
        rutReceptorEl.setTextContent(rutReceptor);
        car.appendChild(rutReceptorEl);

        Element rucEmisorEl = doc.createElementNS(cfeNS, "DGICFE:RUCEmisor");
        rucEmisorEl.setTextContent(rucEmisor);
        car.appendChild(rucEmisorEl);

        Element idEmisorEl = doc.createElementNS(cfeNS, "DGICFE:Idemisor");
        idEmisorEl.setTextContent(idEmisor);
        car.appendChild(idEmisorEl);

        Element cantCFEEl = doc.createElementNS(cfeNS, "DGICFE:CantCFE");
        cantCFEEl.setTextContent(cantCFE);
        car.appendChild(cantCFEEl);

        Element fechaEl = doc.createElementNS(cfeNS, "DGICFE:Fecha");
        fechaEl.setTextContent(fecha);
        car.appendChild(fechaEl);

        Element certEl = doc.createElementNS(cfeNS, "DGICFE:X509Certificate");
        certEl.setTextContent(certB64);  // Aquí va tu certificado codificado en Base64
        car.appendChild(certEl);

        Node parent = cfe.getParentNode();
        parent.replaceChild(envio, cfe);
        envio.appendChild(cfe);

        return doc;
    }
}
