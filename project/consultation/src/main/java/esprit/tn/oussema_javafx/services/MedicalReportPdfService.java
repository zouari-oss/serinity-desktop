package esprit.tn.oussema_javafx.services;



import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import esprit.tn.oussema_javafx.models.Consultation;
import esprit.tn.oussema_javafx.models.User;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicalReportPdfService {

    public File generate(User patient, List<Consultation> consultations) throws Exception {

        File file = new File(System.getProperty("user.home")
                + "/Desktop/Dossier_" + patient.getFullName().replace(" ", "_") + ".pdf");

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // ===== HEADER =====
        Paragraph title = new Paragraph("RAPPORT MÉDICAL")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);

        document.add(title);
        document.add(new Paragraph(" "));

        // ===== PATIENT INFO =====
        document.add(new Paragraph("Patient : " + patient.getFullName()).setBold());
        document.add(new Paragraph("Téléphone : " + patient.getPhone()));
        document.add(new Paragraph("Email : " + patient.getEmail()));
        document.add(new Paragraph(" "));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // ===== CONSULTATIONS =====
        for(Consultation c : consultations){

            document.add(new Paragraph("Consultation du " + c.getDateConsultation().format(fmt))
                    .setBold()
                    .setFontSize(14)
                    .setFontColor(ColorConstants.BLUE));

            document.add(new Paragraph("Diagnostic : " + safe(c.getDiagnostic())));
            document.add(new Paragraph("Prescription : " + safe(c.getPrescription())));
            document.add(new Paragraph("Notes : " + safe(c.getNotes())));
            document.add(new Paragraph("---------------------------------------------------"));
            document.add(new Paragraph(" "));
        }

        document.close();
        return file;
    }

    private String safe(String s){
        return s==null||s.isBlank()?"-":s;
    }
}
