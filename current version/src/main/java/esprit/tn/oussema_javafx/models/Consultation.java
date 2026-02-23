package esprit.tn.oussema_javafx.models;

import java.time.LocalDateTime;

public class Consultation {
    private int id;
    private int rapportId;
    private int rendezVousId;
    private int doctorId;
    private LocalDateTime dateConsultation;
    private String diagnostic;
    private String prescription;
    private String notes;

    // joins
    private String doctorName;
    private String patientName;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRapportId() { return rapportId; }
    public void setRapportId(int rapportId) { this.rapportId = rapportId; }

    public int getRendezVousId() { return rendezVousId; }
    public void setRendezVousId(int rendezVousId) { this.rendezVousId = rendezVousId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getDateConsultation() { return dateConsultation; }
    public void setDateConsultation(LocalDateTime dateConsultation) { this.dateConsultation = dateConsultation; }

    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
}
