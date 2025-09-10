package com.genomic.model;

import java.time.LocalDateTime;

public class Patient {
    private String patientId;        // generado por el servidor
    private String fullName;
    private String documentId;       // único
    private int age;
    private String sex;              // M o F
    private String contactEmail;
    private LocalDateTime registrationDate;
    private String clinicalNotes;
    private String checksumFasta;    // MD5 o SHA-256
    private long fileSizeBytes;      // tamaño del archivo FASTA
    private boolean active;          // borrado lógico

    // Constructor completo
    public Patient(String patientId, String fullName, String documentId, int age, String sex,
                   String contactEmail, LocalDateTime registrationDate, String clinicalNotes,
                   String checksumFasta, long fileSizeBytes, boolean active) {
        this.patientId = patientId;
        this.fullName = fullName;
        this.documentId = documentId;
        this.age = age;
        this.sex = sex;
        this.contactEmail = contactEmail;
        this.registrationDate = registrationDate;
        this.clinicalNotes = clinicalNotes;
        this.checksumFasta = checksumFasta;
        this.fileSizeBytes = fileSizeBytes;
        this.active = active;
    }

    // Builder interno
    public static class Builder {
        private String patientId;
        private String fullName;
        private String documentId;
        private int age;
        private String sex;
        private String contactEmail;
        private LocalDateTime registrationDate = LocalDateTime.now(); // por defecto
        private String clinicalNotes = "";
        private String checksumFasta = "";
        private long fileSizeBytes = 0;
        private boolean active = true;

        public Builder patientId(String patientId) {
            this.patientId = patientId;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder documentId(String documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Builder sex(String sex) {
            this.sex = sex;
            return this;
        }

        public Builder contactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
            return this;
        }

        public Builder registrationDate(LocalDateTime registrationDate) {
            this.registrationDate = registrationDate;
            return this;
        }

        public Builder clinicalNotes(String clinicalNotes) {
            this.clinicalNotes = clinicalNotes;
            return this;
        }

        public Builder checksumFasta(String checksumFasta) {
            this.checksumFasta = checksumFasta;
            return this;
        }

        public Builder fileSizeBytes(long fileSizeBytes) {
            this.fileSizeBytes = fileSizeBytes;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Patient build() {
            return new Patient(patientId, fullName, documentId, age, sex, contactEmail,
                               registrationDate, clinicalNotes, checksumFasta, fileSizeBytes, active);
        }
    }

    // Getters y setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public String getClinicalNotes() { return clinicalNotes; }
    public void setClinicalNotes(String clinicalNotes) { this.clinicalNotes = clinicalNotes; }

    public String getChecksumFasta() { return checksumFasta; }
    public void setChecksumFasta(String checksumFasta) { this.checksumFasta = checksumFasta; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId='" + patientId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", documentId='" + documentId + '\'' +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", registrationDate=" + registrationDate +
                ", clinicalNotes='" + clinicalNotes + '\'' +
                ", checksumFasta='" + checksumFasta + '\'' +
                ", fileSizeBytes=" + fileSizeBytes +
                ", active=" + active +
                '}';
    }
}
