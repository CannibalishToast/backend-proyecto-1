package com.genomic.protocol;

import java.util.HashMap;
import java.util.Map;

public class Protocol {
    // Constantes de comandos
    public static final String CREATE_PATIENT = "CREATE_PATIENT";
    public static final String GET_PATIENT = "GET_PATIENT";
    public static final String UPLOAD_FASTA = "UPLOAD_FASTA";

    /**
     * Extrae el comando de un mensaje recibido
     */
    public static String extractCommand(String rawMessage) {
        for (String line : rawMessage.split("\n")) {
            if (line.startsWith("COMMAND:")) {
                return line.replace("COMMAND:", "").trim();
            }
        }
        return null;
    }

    /**
     * Convierte pares clave=valor en un Map
     */
    public static Map<String, String> parseKeyValues(String rawMessage) {
        Map<String, String> data = new HashMap<>();
        for (String line : rawMessage.split("\n")) {
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                data.put(parts[0].trim(), parts[1].trim());
            }
        }
        return data;
    }
}
