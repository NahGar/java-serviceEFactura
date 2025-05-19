package org.ngarcia.serviceEFactura.services;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

import java.io.StringWriter;

public class EnvioCFEBuilder {

  /**
   * Crea el elemento <DGICFE:EnvioCFE> ya completo, listo para
   * importar en tu SOAP y firmar.
   *
   * @param doc                 El Document donde vas a insertar el wrapper (por ejemplo, el SOAP Body)
   * @param cfeElement          El nodo <CFE> ya firmado con SignCFE.sign(...)
   * @param rutReceptor         Texto de RutReceptor
   * @param rucEmisor           Texto de RUCEmisor
   * @param idEmisor            Texto de IdEmisor
   * @param cantCFE             Cantidad de CFE
   * @param fecha               Fecha en formato ISO
   * @param x509CertificateB64  Certificado en Base64
   * @return El Element <DGICFE:EnvioCFE> listo para insertar
   */
  public static Element createEnvioCFEElement(
          Document doc,
          Element cfeElement,
          String rutReceptor,
          String rucEmisor,
          String idEmisor,
          int cantCFE,
          String fecha,
          String x509CertificateB64
  ) {
    // 1) Crear <DGICFE:EnvioCFE>
    Element envio = doc.createElementNS(
            "http://cfe.dgi.gub.uy", "DGICFE:EnvioCFE"
    );
    envio.setAttribute("version", "1.0");
    envio.setAttributeNS(
            "http://www.w3.org/2001/XMLSchema-instance",
            "xsi:schemaLocation",
            "http://cfe.dgi.gub.uy EnvioCFE_v1.11.xsd"
    );
    envio.setAttribute("xmlns:DGICFE", "http://cfe.dgi.gub.uy");
    envio.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

    // 2) Carátula
    Element car = doc.createElementNS("http://cfe.dgi.gub.uy", "DGICFE:Caratula");
    car.setAttribute("version", "1.0");
    envio.appendChild(car);

    appendText(doc, car, "DGICFE:RutReceptor", rutReceptor);
    appendText(doc, car, "DGICFE:RUCEmisor",   rucEmisor);
    appendText(doc, car, "DGICFE:Idemisor",    idEmisor);
    appendText(doc, car, "DGICFE:CantCFE",     String.valueOf(cantCFE));
    appendText(doc, car, "DGICFE:Fecha",       fecha);
    appendText(doc, car, "DGICFE:X509Certificate", x509CertificateB64);

    // 3) Importar el <CFE> ya firmado
    Node imported = doc.importNode(cfeElement, true);
    envio.appendChild(imported);

    return envio;
  }

  private static void appendText(
          Document doc, Element parent, String qName, String text
  ) {
    Element e = doc.createElementNS("http://cfe.dgi.gub.uy", qName);
    e.setTextContent(text);
    parent.appendChild(e);
  }
}
