package esprit.tn.oussema_javafx.controllers.doctor;

import esprit.tn.oussema_javafx.Router;
import esprit.tn.oussema_javafx.models.User;
import esprit.tn.oussema_javafx.services.DoctorService;
import esprit.tn.oussema_javafx.utils.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
//interface patient ychouf feh list doctor w recherche w jaw hetheka
public class DoctorListController {

    @FXML private FlowPane cardsPane;
    @FXML private TextField searchField;
    @FXML private VBox emptyBox;

    private final DoctorService service = new DoctorService();

    @FXML
    public void initialize(){
        refresh();

        searchField.textProperty().addListener((obs,o,n)-> refresh());
    }



    @FXML
    public void refresh(){

        List<User> doctors = service.findAllDoctors();

        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        // découper la recherche en mots
        String[] keywords = q.split("\\s+");

        cardsPane.getChildren().clear();

        List<User> filteredDoctors = doctors.stream()

                .filter(d -> {

                    // si vide → tout afficher
                    if(q.isBlank()) return true;

                    // champs médecin
                    String fullName   = safe(d.getFullName());
                    String speciality = safe(d.getSpeciality());
                    String phone      = safe(d.getPhone());
                    String address    = safe(d.getAddress());
                    String category   = safe(d.getSpeciality()); // <-- IMPORTANT (ajoute ce champ si existe)
                    String city       = safe(d.getAddress());     // <-- ville

                    // concaténer tous les champs
                    String searchable = (fullName + " " + speciality + " " + phone + " " + address + " " + category + " " + city).toLowerCase();

                    // chaque mot doit exister
                    for(String word : keywords){
                        if(!searchable.contains(word))
                            return false;
                    }
                    return true;
                })

                // tri intelligent : spécialité puis nom
                .sorted((d1, d2) -> {

                    String s1 = safe(d1.getSpeciality());
                    String s2 = safe(d2.getSpeciality());

                    int specCompare = s1.compareToIgnoreCase(s2);
                    if(specCompare != 0) return specCompare;

                    return safe(d1.getFullName()).compareToIgnoreCase(safe(d2.getFullName()));
                })

                .toList();

        filteredDoctors.forEach(d ->
                cardsPane.getChildren().add(createCard(d))
        );

        emptyBox.setVisible(filteredDoctors.isEmpty());
    }
    private String safe(String s){
        return s == null ? "" : s.trim().toLowerCase();
    }

    private VBox createCard(User d){

        VBox card = new VBox(12);
        card.getStyleClass().add("doctor-card");
        card.setPrefWidth(300);

        Label name = new Label(d.getFullName());
        name.getStyleClass().add("doctor-name");

        Label speciality = new Label(d.getSpeciality()==null?"Médecin généraliste":d.getSpeciality());
        speciality.getStyleClass().add("doctor-speciality");

        Label phone = new Label("☎ " + d.getPhone());
        phone.getStyleClass().add("doctor-phone");

        HBox actions = new HBox(10);
        Label address = new Label("📍 " + (d.getAddress()==null?"Adresse inconnue":d.getAddress()));
        address.getStyleClass().add("doctor-address");

        Button consult = new Button("Voir profil");
        consult.getStyleClass().add("secondary-btn");

        consult.setOnAction(e->{
            AppSession.setSelectedDoctorId(d.getId());
            Router.go("/fxml/doctor/doctor_details.fxml","Doctor Details");
        });

        Button rdv = new Button("Prendre RDV");
        rdv.getStyleClass().add("primary-btn");

        rdv.setOnAction(e->{
            AppSession.setSelectedDoctorId(d.getId());
            Router.go("/fxml/rdv/rdv_form.fxml","Prendre Rendez-vous");
        });

        actions.getChildren().addAll(consult,rdv);

        card.getChildren().addAll(name,speciality,phone,address,actions);
        return card;
    }
    @FXML
    public void goMyAppointments(){
        Router.go("/fxml/rdv/rdv_list.fxml","Mes Rendez-vous");
    }

}
