package esprit.tn.oussema_javafx.models;

import java.time.LocalDate;

public class RapportMedical {
    private int id;
    private int patientId;
    private LocalDate dateCreation;
    private String resumeGeneral;

    // join
    private String patientName;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public String getResumeGeneral() { return resumeGeneral; }
    public void setResumeGeneral(String resumeGeneral) { this.resumeGeneral = resumeGeneral; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
}
