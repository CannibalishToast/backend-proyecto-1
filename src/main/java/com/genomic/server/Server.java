package com.genomic.server;

import com.genomic.protocol.Protocol;
import com.genomic.protocol.Messages;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

public class Server {
    public static void main(String[] args) throws Exception {
        int port = 4443;

        // Cargar keystore
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("src/main/java/com/genomic/server/ssl/serverkeystore.jks"),
                "password".toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "password".toCharArray());

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(kmf.getKeyManagers(), null, null);

        SSLServerSocketFactory ssf = sc.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);

        System.out.println("✅ Servidor SSL escuchando en puerto " + port);

        while (true) {
            try (SSLSocket client = (SSLSocket) serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null && !line.equals("END")) {
                    sb.append(line).append("\n");
                }

                String msg = sb.toString();
                System.out.println("📩 Mensaje recibido:\n" + msg);

                String command = Protocol.extractCommand(msg);
                if (Protocol.CREATE_PATIENT.equals(command)) {
                    out.println(Messages.ok("Paciente creado correctamente"));
                } else {
                    out.println(Messages.error(400, "Comando no reconocido"));
                }
            }
        }
    }
}

