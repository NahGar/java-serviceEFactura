package org.ngarcia.serviceEFactura.services;

import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.namespace.QName;
import java.util.Set;

public class LoggingSOAPHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        try {
            Boolean isOutbound = (Boolean) context.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY);
            System.out.println("\n------ " + (isOutbound ? "Outbound" : "Inbound") + " SOAP Message ------");
            context.getMessage().writeTo(System.out);
            System.out.println("\n---------------------------------------------");
        } catch (Exception e) {
            System.out.println("Error al imprimir SOAP: " + e.getMessage());
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return handleMessage(context);
    }

    @Override
    public void close(javax.xml.ws.handler.MessageContext context) {}

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}

