package com.serinity.consultationcontrol.model;

import java.time.LocalDateTime;

public class RendezVous {
    private int id;
    private String patientId;
    private String doctorId;
    private String motif;
    private LocalDateTime dateTime;
    private RdvStatus status;
    private LocalDateTime proposedDateTime;
    private String doctorNote;
    private String description;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // affichage (join)
    private String patientName;
    private String doctorName;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public RdvStatus getStatus() { return status; }
    public void setStatus(RdvStatus status) { this.status = status; }

    public LocalDateTime getProposedDateTime() { return proposedDateTime; }
    public void setProposedDateTime(LocalDateTime proposedDateTime) { this.proposedDateTime = proposedDateTime; }

    public String getDoctorNote() { return doctorNote; }
    public void setDoctorNote(String doctorNote) { this.doctorNote = doctorNote; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
}
