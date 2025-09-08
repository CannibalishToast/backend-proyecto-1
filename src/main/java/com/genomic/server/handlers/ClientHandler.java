package com.genomic.server.handlers;

import com.genomic.protocol.Protocol;
import com.genomic.protocol.Messages;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ClientHandler implements Runnable {
    private SSLSocket client;

    public ClientHandler(SSLSocket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true)
        ) {
            String line;
            StringBuilder sb = new StringBuilder();

            while (true) {
                sb.setLength(0); // limpiar buffer para cada mensaje

                // Leer hasta END o desconexión
                while ((line = in.readLine()) != null) {
                    if (line.equals("END")) break;
                    sb.append(line).append("\n");
                }

                if (sb.length() == 0) {
                    System.out.println("❌ Cliente desconectado.");
                    break; // el cliente cerró conexión
                }

                String msg = sb.toString();
                System.out.println("📩 Mensaje recibido:\n" + msg);

                // Procesar comando
                String command = Protocol.extractCommand(msg);
                if (Protocol.CREATE_PATIENT.equals(command)) {
                    out.println(Messages.ok("Paciente creado correctamente"));
                } else if (Protocol.PING.equals(command)) {
                    out.println(Messages.ok("PONG"));
                } else {
                    out.println(Messages.error(400, "Comando no reconocido"));
                }
            }

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
