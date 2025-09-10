package com.genomic.client;

import javax.net.ssl.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 4443;

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
            System.out.println("3. Eliminar paciente");
            System.out.println("4. Actualizar paciente");
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

                System.out.print("Ruta del archivo FASTA: ");
                String filePath = scanner.nextLine();

                StringBuilder fastaContent = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                    String l;
                    while ((l = br.readLine()) != null) {
                        fastaContent.append(l).append("\n");
                    }
                }

                msg = "COMMAND: CREATE_PATIENT\n" +
                        "full_name=" + fullName + "\n" +
                        "document_id=" + documentId + "\n" +
                        "age=" + age + "\n" +
                        "sex=" + sex + "\n" +
                        "email=" + email + "\n" +
                        "clinical_notes=" + notes + "\n" +
                        "fasta_content=" + fastaContent.toString().replace("\n", "\\n") + "\n" +
                        "END\n";

            } else if (option == 2) {
                System.out.print("Ingrese patient_id: ");
                String patientId = scanner.nextLine();

                msg = "COMMAND: GET_PATIENT\n" +
                        "patient_id=" + patientId + "\n" +
                        "END\n";

            } else if (option == 3) {
                System.out.print("Ingrese patient_id a eliminar: ");
                String patientId = scanner.nextLine();

                msg = "COMMAND: DELETE_PATIENT\n" +
                        "patient_id=" + patientId + "\n" +
                        "END\n";

            } else if (option == 4) {
                System.out.print("Ingrese patient_id a actualizar: ");
                String patientId = scanner.nextLine();

                System.out.println("Ingrese nuevos datos (deje vacío para no cambiar):");

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

                System.out.print("¿Desea subir un nuevo archivo FASTA? (s/n): ");
                String subirFasta = scanner.nextLine();

                StringBuilder fastaContent = new StringBuilder();
                if (subirFasta.equalsIgnoreCase("s")) {
                    System.out.print("Ruta del archivo FASTA: ");
                    String filePath = scanner.nextLine();

                    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                        String l;
                        while ((l = br.readLine()) != null) {
                            fastaContent.append(l).append("\n");
                        }
                    }
                }

                msg = "COMMAND: UPDATE_PATIENT\n" +
                        "patient_id=" + patientId + "\n";

                if (!fullName.isBlank()) msg += "full_name=" + fullName + "\n";
                if (!documentId.isBlank()) msg += "document_id=" + documentId + "\n";
                if (!age.isBlank()) msg += "age=" + age + "\n";
                if (!sex.isBlank()) msg += "sex=" + sex + "\n";
                if (!email.isBlank()) msg += "email=" + email + "\n";
                if (!notes.isBlank()) msg += "clinical_notes=" + notes + "\n";
                if (fastaContent.length() > 0) msg += "fasta_content=" + fastaContent.toString().replace("\n", "\\n") + "\n";

                msg += "END\n";
            }

            out.println(msg);

            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("📨 " + response);
                if (response.equals("END")) break;
            }
        }
    }
}

