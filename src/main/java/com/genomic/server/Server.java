package com.genomic.server;

import com.genomic.server.handlers.ClientHandler;
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

        // Aceptar múltiples clientes
        while (true) {
            SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
            System.out.println("👥 Cliente conectado.");
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }
}
