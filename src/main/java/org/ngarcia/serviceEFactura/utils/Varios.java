package org.ngarcia.serviceEFactura.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Varios {

    public static String formatearFecha(LocalDateTime fecha) {
      // Convertir a ZonedDateTime con la zona horaria del sistema
      ZonedDateTime zonedDateTime = fecha.atZone(ZoneId.systemDefault());
      return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
    }

}
