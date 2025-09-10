package com.genomic.server;

import com.genomic.server.handlers.ClientHandler;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.logging.*;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    static {
        try {
            LogManager.getLogManager().reset();

            // Handler para archivo
            FileHandler fh = new FileHandler("server.log", true);
            fh.setFormatter(new SimpleFormatter());

            // Handler para consola
            ConsoleHandler ch = new ConsoleHandler();
            ch.setFormatter(new SimpleFormatter());

            logger.addHandler(fh);
            logger.addHandler(ch);
            logger.setLevel(Level.INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        logger.info("Servidor SSL escuchando en puerto " + port);

        // Aceptar múltiples clientes
        while (true) {
            SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
            logger.info("Cliente conectado desde " + clientSocket.getInetAddress());
            new Thread(new ClientHandler(clientSocket, logger)).start();
        }
    }
}
