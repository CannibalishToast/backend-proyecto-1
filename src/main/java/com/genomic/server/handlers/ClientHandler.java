package com.genomic.server.handlers;

import com.genomic.protocol.Protocol;
import com.genomic.protocol.Messages;
import com.genomic.model.Patient;
import com.genomic.repository.PatientRepository;
import com.genomic.repository.DiseaseRepository;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private SSLSocket client;
    private PatientRepository repository = new PatientRepository();
    private DiseaseRepository diseaseRepo = new DiseaseRepository();
    private static final String DETECTIONS_FILE = "data/detections_report.csv";

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
                sb.setLength(0);

                while ((line = in.readLine()) != null) {
                    if (line.equals("END")) break;
                    sb.append(line).append("\n");
                }

                if (sb.length() == 0) {
                    System.out.println("❌ Cliente desconectado.");
                    break;
                }

                String msg = sb.toString();
                System.out.println("📩 Mensaje recibido:\n" + msg);

                String command = Protocol.extractCommand(msg);
                if (Protocol.CREATE_PATIENT.equals(command)) {
                    handleCreatePatient(msg, out);
                } else if (Protocol.GET_PATIENT.equals(command)) {
                    handleGetPatient(msg, out);
                } else if (Protocol.DELETE_PATIENT.equals(command)) {
                    handleDeletePatient(msg, out);
                } else if (Protocol.UPDATE_PATIENT.equals(command)) {
                    handleUpdatePatient(msg, out);
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

            if (!data.containsKey("full_name") || !data.containsKey("document_id") ||
                !data.containsKey("age") || !data.containsKey("sex") || !data.containsKey("email")) {
                out.println(Messages.error(422, "Faltan campos obligatorios"));
                return;
            }

            String patientId = "P-" + UUID.randomUUID().toString().substring(0, 8);

            String fastaContent = data.get("fasta_content");
            String checksum = "";
            long fileSize = 0;

            if (fastaContent != null && !fastaContent.isBlank()) {
                fastaContent = fastaContent.replace("\\n", "\n");
                if (!validateAndSaveFasta(patientId, fastaContent, out)) return;

                checksum = calculateChecksum(fastaContent);
                fileSize = fastaContent.getBytes().length;

                // 🔬 comparar contra enfermedades
                checkDiseases(patientId, fastaContent, out);
            }

            Patient patient = new Patient.Builder()
                    .patientId(patientId)
                    .fullName(data.get("full_name"))
                    .documentId(data.get("document_id"))
                    .age(Integer.parseInt(data.get("age")))
                    .sex(data.get("sex"))
                    .contactEmail(data.get("email"))
                    .registrationDate(LocalDateTime.now())
                    .clinicalNotes(data.getOrDefault("clinical_notes", ""))
                    .checksumFasta(checksum)
                    .fileSizeBytes(fileSize)
                    .active(true)
                    .build();

            repository.save(patient);

            out.println(Messages.ok("Paciente creado con ID " + patientId));
            System.out.println("✅ Paciente guardado en CSV con ID " + patientId);

        } catch (IllegalArgumentException e) {
            out.println(Messages.error(409, e.getMessage()));
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
            response.append("active=").append(p.isActive()).append("\n");
            response.append("END\n");

            out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            out.println(Messages.error(500, "Error interno al obtener paciente"));
        }
    }

    private void handleDeletePatient(String msg, PrintWriter out) {
        try {
            Map<String, String> data = Protocol.parseKeyValues(msg);
            if (!data.containsKey("patient_id")) {
                out.println(Messages.error(422, "Falta el campo patient_id"));
                return;
            }

            String patientId = data.get("patient_id");
            boolean success = repository.deactivateById(patientId);

            if (success) {
                out.println(Messages.ok("Paciente " + patientId + " marcado como inactivo"));
                System.out.println("🗑️ Paciente " + patientId + " desactivado.");
            } else {
                out.println(Messages.error(404, "Paciente no encontrado o ya estaba inactivo"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println(Messages.error(500, "Error interno al eliminar paciente"));
        }
    }

    private void handleUpdatePatient(String msg, PrintWriter out) {
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

            Patient existing = patientOpt.get();

            String fastaContent = data.get("fasta_content");
            String checksum = existing.getChecksumFasta();
            long fileSize = existing.getFileSizeBytes();

            if (fastaContent != null && !fastaContent.isBlank()) {
                fastaContent = fastaContent.replace("\\n", "\n");
                if (!validateAndSaveFasta(patientId, fastaContent, out)) return;

                checksum = calculateChecksum(fastaContent);
                fileSize = fastaContent.getBytes().length;

                // 🔬 comparar contra enfermedades
                checkDiseases(patientId, fastaContent, out);
            }

            Patient updated = new Patient.Builder()
                    .patientId(existing.getPatientId())
                    .fullName(data.getOrDefault("full_name", existing.getFullName()))
                    .documentId(data.getOrDefault("document_id", existing.getDocumentId()))
                    .age(Integer.parseInt(data.getOrDefault("age", String.valueOf(existing.getAge()))))
                    .sex(data.getOrDefault("sex", existing.getSex()))
                    .contactEmail(data.getOrDefault("email", existing.getContactEmail()))
                    .registrationDate(existing.getRegistrationDate())
                    .clinicalNotes(data.getOrDefault("clinical_notes", existing.getClinicalNotes()))
                    .checksumFasta(checksum)
                    .fileSizeBytes(fileSize)
                    .active(existing.isActive())
                    .build();

            boolean success = repository.update(updated);

            if (success) {
                out.println(Messages.ok("Paciente " + patientId + " actualizado correctamente"));
                System.out.println("✏️ Paciente " + patientId + " actualizado.");
            } else {
                out.println(Messages.error(500, "No se pudo actualizar el paciente"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println(Messages.error(500, "Error interno al actualizar paciente"));
        }
    }

    private boolean validateAndSaveFasta(String patientId, String fastaContent, PrintWriter out) {
        try {
            String[] lines = fastaContent.split("\n");
            if (lines.length < 2 || !lines[0].startsWith(">")) {
                out.println(Messages.error(422, "Formato FASTA inválido"));
                return false;
            }
            for (int i = 1; i < lines.length; i++) {
                if (!lines[i].matches("[ACGTN]+")) {
                    out.println(Messages.error(422, "Secuencia contiene caracteres inválidos"));
                    return false;
                }
            }

            File dir = new File("data/patients");
            if (!dir.exists()) dir.mkdirs();

            File fastaFile = new File(dir, patientId + ".fasta");
            try (FileWriter fw = new FileWriter(fastaFile)) {
                fw.write(fastaContent);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            out.println(Messages.error(500, "Error al procesar archivo FASTA"));
            return false;
        }
    }

    private String calculateChecksum(String content) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(content.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private void checkDiseases(String patientId, String fastaContent, PrintWriter out) {
        try {
            String patientSeq = fastaContent.replaceAll("\n", "").replaceAll(">", "");

            var diseases = diseaseRepo.findAll();
            boolean found = false;

            for (DiseaseRepository.Disease d : diseases) {
                String diseaseSeq = diseaseRepo.loadFasta(d.getDiseaseId());
                if (diseaseSeq != null && patientSeq.contains(diseaseSeq)) {
                    found = true;

                    // registrar en detections_report.csv
                    File file = new File(DETECTIONS_FILE);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
                            w.write("patient_id,disease_id,disease_name,timestamp\n");
                        }
                    }
                    try (BufferedWriter w = new BufferedWriter(new FileWriter(file, true))) {
                        String ts = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        w.write(patientId + "," + d.getDiseaseId() + "," + d.getName() + "," + ts + "\n");
                    }

                    out.println(Messages.ok("⚠️ Posible detección: " + d.getName() + " (" + d.getSeverity() + ")"));
                    System.out.println("⚠️ Detección registrada: " + d.getName() + " en paciente " + patientId);
                }
            }

            if (!found) {
                out.println(Messages.ok("No se detectaron enfermedades conocidas."));
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println(Messages.error(500, "Error en comparación de enfermedades"));
        }
    }
}

