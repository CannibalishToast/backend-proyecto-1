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

            boolean running = true;

            while (running) {
                System.out.println("\n=== MENÚ CLIENTE ===");
                System.out.println("1. Crear paciente");
                System.out.println("2. Obtener paciente");
                System.out.println("3. Eliminar paciente");
                System.out.println("4. Actualizar paciente");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                String option = scanner.nextLine();

                String msg = null;

                switch (option) {
                    case "1":
                        System.out.print("Nombre completo: ");
                        String name = scanner.nextLine();
                        System.out.print("Documento: ");
                        String doc = scanner.nextLine();
                        System.out.print("Edad: ");
                        String age = scanner.nextLine();
                        System.out.print("Sexo (M/F): ");
                        String sex = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();

                        System.out.println("Ingrese contenido FASTA (termine con una línea vacía):");
                        StringBuilder fasta = new StringBuilder();
                        while (true) {
                            String line = scanner.nextLine();
                            if (line.isBlank()) break;
                            fasta.append(line).append("\\n"); // escapar \n
                        }

                        msg = "COMMAND: CREATE_PATIENT\n" +
                              "full_name=" + name + "\n" +
                              "document_id=" + doc + "\n" +
                              "age=" + age + "\n" +
                              "sex=" + sex + "\n" +
                              "email=" + email + "\n" +
                              "fasta_content=" + fasta.toString() + "\nEND\n";
                        break;

                    case "2":
                        System.out.print("ID del paciente: ");
                        String idGet = scanner.nextLine();
                        msg = "COMMAND: GET_PATIENT\npatient_id=" + idGet + "\nEND\n";
                        break;

                    case "3":
                        System.out.print("ID del paciente a eliminar: ");
                        String idDel = scanner.nextLine();
                        msg = "COMMAND: DELETE_PATIENT\npatient_id=" + idDel + "\nEND\n";
                        break;

                    case "4":
                        System.out.print("ID del paciente a actualizar: ");
                        String idUpd = scanner.nextLine();
                        System.out.print("Nuevo nombre (enter para dejar igual): ");
                        String newName = scanner.nextLine();
                        System.out.print("Nueva edad (enter para dejar igual): ");
                        String newAge = scanner.nextLine();
                        System.out.println("Nuevo FASTA (enter para omitir, terminar con línea vacía):");
                        StringBuilder newFasta = new StringBuilder();
                        while (true) {
                            String line = scanner.nextLine();
                            if (line.isBlank()) break;
                            newFasta.append(line).append("\\n");
                        }

                        msg = "COMMAND: UPDATE_PATIENT\n" +
                              "patient_id=" + idUpd + "\n";
                        if (!newName.isBlank()) msg += "full_name=" + newName + "\n";
                        if (!newAge.isBlank()) msg += "age=" + newAge + "\n";
                        if (newFasta.length() > 0) msg += "fasta_content=" + newFasta.toString() + "\n";
                        msg += "END\n";
                        break;

                    case "0":
                        running = false;
                        continue;

                    default:
                        System.out.println("Opción inválida.");
                        continue;
                }

                if (msg != null) {
                    out.println(msg);

                    String response;
                    System.out.println("--- Respuesta del servidor ---");
                    while ((response = in.readLine()) != null) {
                        if (response.equals("END")) break;
                        System.out.println(response);
                    }
                    System.out.println("-------------------------------");
                }
            }
        }
    }
}
