package com.genomic.repository;

import com.genomic.model.Patient;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PatientRepository {
    private static final String FILE_PATH = "data/patients.csv";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public PatientRepository() {
        try {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
                try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                    writer.write("patient_id,full_name,document_id,age,sex,contact_email,registration_date,clinical_notes,checksum_fasta,file_size_bytes,active\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void save(Patient patient) throws IOException {
        if (existsByDocumentId(patient.getDocumentId())) {
            throw new IllegalArgumentException("Ya existe un paciente con el mismo document_id");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH), StandardOpenOption.APPEND)) {
            writer.write(toCsvLine(patient));
            writer.write("\n");
        }
    }

    public boolean existsByDocumentId(String documentId) {
        return findAll().stream().anyMatch(p -> p.getDocumentId().equals(documentId));
    }

    public List<Patient> findAll() {
        List<Patient> patients = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            String line;
            reader.readLine(); // cabecera
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 11) continue;
                patients.add(new Patient(
                        parts[0],
                        parts[1],
                        parts[2],
                        Integer.parseInt(parts[3]),
                        parts[4],
                        parts[5],
                        LocalDateTime.parse(parts[6], FORMATTER),
                        parts[7],
                        parts[8],
                        Long.parseLong(parts[9]),
                        Boolean.parseBoolean(parts[10])
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return patients;
    }

    public synchronized boolean deactivateById(String patientId) {
        List<Patient> patients = findAll();
        boolean updated = false;

        for (Patient p : patients) {
            if (p.getPatientId().equals(patientId) && p.isActive()) {
                p.setActive(false);
                updated = true;
                break;
            }
        }
        if (updated) rewriteCsv(patients);
        return updated;
    }

    public synchronized boolean update(Patient updatedPatient) {
        List<Patient> patients = findAll();
        boolean updated = false;

        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getPatientId().equals(updatedPatient.getPatientId())) {
                patients.set(i, updatedPatient);
                updated = true;
                break;
            }
        }
        if (updated) rewriteCsv(patients);
        return updated;
    }

    private void rewriteCsv(List<Patient> patients) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH))) {
            writer.write("patient_id,full_name,document_id,age,sex,contact_email,registration_date,clinical_notes,checksum_fasta,file_size_bytes,active\n");
            for (Patient p : patients) {
                writer.write(toCsvLine(p));
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toCsvLine(Patient patient) {
        return String.join(",",
                patient.getPatientId(),
                patient.getFullName(),
                patient.getDocumentId(),
                String.valueOf(patient.getAge()),
                patient.getSex(),
                patient.getContactEmail(),
                patient.getRegistrationDate().format(FORMATTER),
                patient.getClinicalNotes().replace(",", ";"),
                patient.getChecksumFasta(),
                String.valueOf(patient.getFileSizeBytes()),
                String.valueOf(patient.isActive())
        );
    }
}
