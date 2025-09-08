package com.genomic.client;

import javax.net.ssl.*;
import java.io.*;

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
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String msg = "COMMAND: CREATE_PATIENT\nfull_name=Jane Doe\nage=25\nsex=F\nemail=jane@example.com\nEND\n";
            out.println(msg);

            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("📨 Respuesta del servidor: " + response);
            }
        }
    }
}
