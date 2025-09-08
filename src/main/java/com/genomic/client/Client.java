package com.genomic.client;

import javax.net.ssl.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 4443;

        // TrustManager permisivo (acepta cualquier certificado)
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
        }}, new java.security.SecureRandom());

        SSLSocketFactory factory = sc.getSocketFactory();

        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("=== MENÚ CLIENTE ===");
            System.out.println("1. Crear paciente");
            System.out.println("2. Obtener paciente");
            System.out.print("Seleccione una opción: ");
            int option = Integer.parseInt(scanner.nextLine());

            String msg = "";

            if (option == 1) {
                System.out.print("Nombre completo: ");
                String fullName = scanner.nextLine();
                System.out.print("Documento ID: ");
                String documentId = scanner.nextLine();
                System.out.print("Edad: ");
                String age = scanner.nextLine();
                System.out.print("Sexo (M/F): ");
                String sex = scanner.nextLine();
                System.out.print("Email: ");
                String email = scanner.nextLine();
                System.out.print("Notas clínicas: ");
                String notes = scanner.nextLine();

                msg = "COMMAND: CREATE_PATIENT\n" +
                        "full_name=" + fullName + "\n" +
                        "document_id=" + documentId + "\n" +
                        "age=" + age + "\n" +
                        "sex=" + sex + "\n" +
                        "email=" + email + "\n" +
                        "clinical_notes=" + notes + "\n" +
                        "checksum_fasta=abc123def456\n" +
                        "file_size_bytes=1024\n" +
                        "END\n";

            } else if (option == 2) {
                System.out.print("Ingrese patient_id: ");
                String patientId = scanner.nextLine();

                msg = "COMMAND: GET_PATIENT\n" +
                        "patient_id=" + patientId + "\n" +
                        "END\n";
            }

            out.println(msg);

            // Recibir respuesta
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("📨 " + response);
                if (response.equals("END")) break;
            }
        }
    }
}
