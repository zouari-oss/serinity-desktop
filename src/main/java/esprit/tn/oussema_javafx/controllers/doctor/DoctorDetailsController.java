package esprit.tn.oussema_javafx.controllers.doctor;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import esprit.tn.oussema_javafx.Router;
import esprit.tn.oussema_javafx.models.User;
import esprit.tn.oussema_javafx.services.DoctorService;
import esprit.tn.oussema_javafx.utils.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class DoctorDetailsController {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label specialityLabel;
    @FXML private Label addressLabel;
    @FXML
    private ImageView qrImage;

    private final DoctorService service = new DoctorService();
    private User doctor;

    @FXML
    public void initialize(){

        int id = AppSession.getSelectedDoctorId();
        doctor = service.findById(id);

        if(doctor == null){
            Router.go("/fxml/doctor/doctor_list.fxml","Doctors");
            return;
        }
        addressLabel.setText(doctor.getAddress()==null?"Adresse non renseignée":doctor.getAddress());

        nameLabel.setText(doctor.getFullName());
        emailLabel.setText(doctor.getEmail());
        phoneLabel.setText(doctor.getPhone());
        specialityLabel.setText(doctor.getSpeciality()==null?"Médecin généraliste":doctor.getSpeciality());
        generateQRCode(doctor);

    }

    @FXML
    public void takeRdv(){
        // on garde le doctorId dans session
        Router.go("/fxml/rdv/rdv_form.fxml","Prendre Rendez-vous");
    }

    @FXML
    public void back(){
        Router.go("/fxml/doctor/doctor_list.fxml","Doctors");
    }
    private String generateVCard(User d){

        String phone = d.getPhone()==null?"":d.getPhone();
        if(!phone.startsWith("+216"))
            phone = "+216" + phone;

        return "BEGIN:VCARD\n" +
                "VERSION:3.0\n" +
                "N:" + d.getFullName() + "\n" +
                "FN:" + d.getFullName() + "\n" +
                "ORG:Cabinet Medical\n" +
                "TITLE:" + (d.getSpeciality()==null?"Médecin":d.getSpeciality()) + "\n" +
                "TEL;TYPE=CELL:" + phone + "\n" +
                "EMAIL:" + (d.getEmail()==null?"":d.getEmail()) + "\n" +
                "ADR:;;" + (d.getAddress()==null?"":d.getAddress()) + ";;;;\n" +
                "END:VCARD";
    }
    private void generateQRCode(User d){
        try{

            String vcard = generateVCard(d);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(vcard, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", os);

            Image fxImage = new Image(new ByteArrayInputStream(os.toByteArray()));
            qrImage.setImage(fxImage);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
