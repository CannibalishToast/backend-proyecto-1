package com.genomic.model;

import java.time.LocalDateTime;

public class Patient {
    private String patientId;        //generado automaticamente
    private String fullName;
    private String documentId;       //unico
    private int age;
    private String sex;              // M/F
    private String contactEmail;
    private LocalDateTime registrationDate;
    private String clinicalNotes;
    private String checksumFasta;    // MD5/SHA-256
    private long fileSizeBytes;      //tamaño del archivo FASTA

    public Patient(String patientId, String fullName, String documentId, int age, String sex,
                   String contactEmail, LocalDateTime registrationDate, String clinicalNotes,
                   String checksumFasta, long fileSizeBytes) {
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
    }

    //getters/setters
    public String getPatientId() { return patientId; }
    public String getFullName() { return fullName; }
    public String getDocumentId() { return documentId; }
    public int getAge() { return age; }
    public String getSex() { return sex; }
    public String getContactEmail() { return contactEmail; }
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public String getClinicalNotes() { return clinicalNotes; }
    public String getChecksumFasta() { return checksumFasta; }
    public long getFileSizeBytes() { return fileSizeBytes; }
}
