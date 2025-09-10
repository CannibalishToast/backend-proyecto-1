package com.genomic.repository;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DiseaseRepository {
    private static final String FILE_PATH = "data/diseases.csv";

    public static class Disease {
        private String diseaseId;
        private String name;
        private String severity;

        public Disease(String diseaseId, String name, String severity) {
            this.diseaseId = diseaseId;
            this.name = name;
            this.severity = severity;
        }

        public String getDiseaseId() { return diseaseId; }
        public String getName() { return name; }
        public String getSeverity() { return severity; }
    }

    public List<Disease> findAll() {
        List<Disease> diseases = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH))) {
            String line;
            reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 3) continue;
                diseases.add(new Disease(parts[0], parts[1], parts[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return diseases;
    }

    public String loadFasta(String diseaseId) {
        File fastaFile = new File("data/diseases/" + diseaseId + ".fasta");
        if (!fastaFile.exists()) return null;

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fastaFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(">")) sb.append(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
