package org.ngarcia.serviceEFactura.services;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.*;

import jakarta.xml.soap.*;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import org.ngarcia.serviceEFactura.utils.LogObject;
import org.w3c.dom.Document;

public class WSSecurityHeaderSOAPHandler implements SOAPHandler<SOAPMessageContext> {

    private final X509Certificate certificate;
    private final PrivateKey privateKey;

    public WSSecurityHeaderSOAPHandler(X509Certificate certificate, PrivateKey privateKey) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean isOutbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (isOutbound) {
            try {
                SOAPMessage soapMessage = context.getMessage();
                SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();

                // Agregar namespaces necesarios
                envelope.addNamespaceDeclaration("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
                envelope.addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
                envelope.addNamespaceDeclaration("ds", "http://www.w3.org/2000/09/xmldsig#");

                SOAPHeader header = envelope.getHeader();
                if (header == null) {
                    header = envelope.addHeader();
                }

                // Crear elemento Security
                SOAPHeaderElement security = header.addHeaderElement(
                        new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                                "Security", "wsse"));
                security.setMustUnderstand(true);

                // Agregar BinarySecurityToken
                SOAPElement binarySecurityToken = security.addChildElement(
                        new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                                "BinarySecurityToken", "wsse"));
                binarySecurityToken.setAttribute("EncodingType",
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
                binarySecurityToken.setAttribute("ValueType",
                        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
                binarySecurityToken.setAttribute("wsu:Id", "X509Token");

                String base64Cert = Base64.getEncoder().encodeToString(certificate.getEncoded());
                binarySecurityToken.setTextContent(base64Cert);

                // Preparar el body para la firma
                SOAPBody body = envelope.getBody();
                body.addAttribute(new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
                        "Id", "wsu"), "body");

                // Configurar la firma XML
                XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");

                // Crear referencia al body
                Reference ref = sigFactory.newReference(
                        "#body",
                        sigFactory.newDigestMethod(DigestMethod.SHA1, null),
                        Collections.singletonList(
                                sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
                        null,
                        null);

                // Configurar SignedInfo
                SignedInfo signedInfo = sigFactory.newSignedInfo(
                        sigFactory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE,
                                (C14NMethodParameterSpec) null),
                        sigFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                        Collections.singletonList(ref),
                        null  // ID
                );

                // Configurar KeyInfo
                KeyInfoFactory keyInfoFactory = sigFactory.getKeyInfoFactory();
                List<XMLStructure> keyInfoContent = new ArrayList<>();

                // Crear SecurityTokenReference
                KeyValue keyValue = keyInfoFactory.newKeyValue(certificate.getPublicKey());
                KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyValue));

                // Configurar contexto de firma
                DOMSignContext signContext = new DOMSignContext(privateKey, envelope);
                signContext.setDefaultNamespacePrefix("ds");

                // Para manejar namespaces, asegúrate de que están declarados en el envelope
                envelope.addNamespaceDeclaration("ds", "http://www.w3.org/2000/09/xmldsig#");
                envelope.addNamespaceDeclaration("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
                envelope.addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

                // Crear y aplicar la firma
                XMLSignature signature = sigFactory.newXMLSignature(signedInfo, keyInfo);
                signature.sign(signContext);

                soapMessage.saveChanges();

                // Para depuración
                //ByteArrayOutputStream out = new ByteArrayOutputStream();
                //soapMessage.writeTo(out);
                //System.out.println("SOAP Message:\n" + out.toString("UTF-8"));

            } catch (Exception e) {
                throw new RuntimeException("Error firmando el mensaje SOAP: " + e.getMessage(), e);
            }
        }
        return true;
    }

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {}

    // Clase auxiliar para crear SecurityTokenReference como KeyInfo
    static class SecurityTokenReference {
        private final String referenceURI;
        private final Document doc;

        public SecurityTokenReference(String referenceURI, Document doc) {
            this.referenceURI = referenceURI;
            this.doc = doc;
        }

        public javax.xml.crypto.XMLStructure getElement() {
            return new javax.xml.crypto.dom.DOMStructure(createElement());
        }

        private org.w3c.dom.Element createElement() {
            org.w3c.dom.Element str = doc.createElementNS(
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                    "wsse:SecurityTokenReference");

            org.w3c.dom.Element reference = doc.createElementNS(
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                    "wsse:Reference");
            reference.setAttribute("URI", "#" + referenceURI);
            reference.setAttribute("ValueType",
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");

            str.appendChild(reference);
            return str;
        }
    }
}