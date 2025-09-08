package com.genomic.server.handlers;

import com.genomic.protocol.Protocol;
import com.genomic.protocol.Messages;
import com.genomic.model.Patient;
import com.genomic.repository.PatientRepository;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private SSLSocket client;
    private PatientRepository repository = new PatientRepository();

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
                    handleCreatePatient(msg, out);
                } else if (Protocol.GET_PATIENT.equals(command)) {
                    handleGetPatient(msg, out);
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

    private void handleCreatePatient(String msg, PrintWriter out) {
        try {
            Map<String, String> data = Protocol.parseKeyValues(msg);

            // Validar campos mínimos
            if (!data.containsKey("full_name") || !data.containsKey("document_id") ||
                !data.containsKey("age") || !data.containsKey("sex") || !data.containsKey("email")) {
                out.println(Messages.error(422, "Faltan campos obligatorios"));
                return;
            }

            // Generar ID único
            String patientId = "P-" + UUID.randomUUID().toString().substring(0, 8);

            Patient patient = new Patient(
                    patientId,
                    data.get("full_name"),
                    data.get("document_id"),
                    Integer.parseInt(data.get("age")),
                    data.get("sex"),
                    data.get("email"),
                    LocalDateTime.now(),
                    data.getOrDefault("clinical_notes", ""),
                    data.getOrDefault("checksum_fasta", ""),
                    Long.parseLong(data.getOrDefault("file_size_bytes", "0"))
            );

            repository.save(patient);

            out.println(Messages.ok("Paciente creado con ID " + patientId));
            System.out.println("✅ Paciente guardado en CSV con ID " + patientId);

        } catch (IllegalArgumentException e) {
            out.println(Messages.error(409, e.getMessage())); // por ejemplo, doc_id duplicado
        } catch (Exception e) {
            e.printStackTrace();
            out.println(Messages.error(500, "Error interno al crear paciente"));
        }
    }

    private void handleGetPatient(String msg, PrintWriter out) {
        try {
            Map<String, String> data = Protocol.parseKeyValues(msg);
            if (!data.containsKey("patient_id")) {
                out.println(Messages.error(422, "Falta el campo patient_id"));
                return;
            }

            String patientId = data.get("patient_id");
            var patients = repository.findAll();
            var patientOpt = patients.stream()
                    .filter(p -> p.getPatientId().equals(patientId))
                    .findFirst();

            if (patientOpt.isEmpty()) {
                out.println(Messages.error(404, "Paciente no encontrado"));
                return;
            }

            var p = patientOpt.get();
            StringBuilder response = new StringBuilder();
            response.append("STATUS: OK\n");
            response.append("patient_id=").append(p.getPatientId()).append("\n");
            response.append("full_name=").append(p.getFullName()).append("\n");
            response.append("document_id=").append(p.getDocumentId()).append("\n");
            response.append("age=").append(p.getAge()).append("\n");
            response.append("sex=").append(p.getSex()).append("\n");
            response.append("contact_email=").append(p.getContactEmail()).append("\n");
            response.append("registration_date=").append(p.getRegistrationDate()).append("\n");
            response.append("clinical_notes=").append(p.getClinicalNotes()).append("\n");
            response.append("checksum_fasta=").append(p.getChecksumFasta()).append("\n");
            response.append("file_size_bytes=").append(p.getFileSizeBytes()).append("\n");
            response.append("END\n");

            out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            out.println(Messages.error(500, "Error interno al obtener paciente"));
        }
    }
}
