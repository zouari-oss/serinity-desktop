package esprit.tn.oussema_javafx.controllers.doctor;

import esprit.tn.oussema_javafx.Router;
import esprit.tn.oussema_javafx.services.DashboardService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class DashboardController {

    @FXML private Label patientsLabel;
    @FXML private Label todayLabel;
    @FXML private Label pendingLabel;
    @FXML private Label consultLabel;
    @FXML private Label rateLabel;


    private final DashboardService service = new DashboardService();
    private final int doctorId = 2; // à remplacer par doctor connecté
    @FXML private LineChart<String, Number> rdvWeekChart;
    @FXML private LineChart<String, Number> consultChart;
    @FXML private BarChart<String, Number> statusChart;
    @FXML private LineChart<String, Number> monthlyChart;
    @FXML private BarChart<String, Number> patientFreqChart;
    @FXML

    public void initialize(){
        loadStats();

        loadRdvWeekChart();
        loadConsultChart();
        loadStatusChart();
        loadMonthlyChart();
        loadTopPatients();
        applyModernChartStyle(rdvWeekChart);
        applyModernChartStyle(consultChart);
        applyModernChartStyle(monthlyChart);
    }

    private void loadStats(){
        patientsLabel.setText(String.valueOf(service.totalPatients(doctorId)));
        todayLabel.setText(String.valueOf(service.todayAppointments(doctorId)));
        pendingLabel.setText(String.valueOf(service.pendingAppointments(doctorId)));
        consultLabel.setText(String.valueOf(service.totalConsultations(doctorId)));
        rateLabel.setText(String.format("%.1f %%", service.acceptanceRate(doctorId)));
    }


    public void goRdv(ActionEvent event) {
        Router.go("/fxml/doctor/doctor_rdv_list.fxml","Mes RDV");

    }

    public void goConsultations(ActionEvent event) {
        Router.go("/fxml/doctor/doctor_rdv_list.fxml","Mes RDV");

    }

    public void goPatients(ActionEvent event) {
        Router.go("/fxml/doctor/mesPatient.fxml","Mes RDV");


    }
    private void animateSeries(XYChart.Series<String, Number> series){
        int i = 0;
        for (XYChart.Data<String, Number> d : series.getData()) {
            d.getNode().setStyle("-fx-background-radius: 5px;");
        }
    }private void applyModernChartStyle(XYChart<String, Number> chart){
        chart.setLegendVisible(false);
        chart.setAnimated(true);

        chart.lookup(".chart-plot-background").setStyle(
                "-fx-background-color: transparent;"
        );
    }
    private void loadRdvWeekChart(){
        try{
            rdvWeekChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Rendez-vous");

            ResultSet rs = service.rdvLast7Days(doctorId);

            while(rs.next()){
                LocalDate date = rs.getDate("d").toLocalDate();
                String day = date.getDayOfWeek()
                        .getDisplayName(TextStyle.SHORT, Locale.FRANCE);

                int total = rs.getInt("total");

                series.getData().add(new XYChart.Data<>(day, total));
            }

            rdvWeekChart.getData().add(series);
            animateSeries(series);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void loadConsultChart(){
        try{
            consultChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Consultations");

            ResultSet rs = service.consultationsLast7Days(doctorId);

            while(rs.next()){
                LocalDate date = rs.getDate("d").toLocalDate();
                String day = date.getDayOfWeek()
                        .getDisplayName(TextStyle.SHORT, Locale.FRANCE);

                int total = rs.getInt("total");

                series.getData().add(new XYChart.Data<>(day, total));
            }

            consultChart.getData().add(series);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void loadStatusChart(){
        try{
            statusChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Statuts");

            ResultSet rs = service.statusDistribution(doctorId);

            while(rs.next()){
                String status = rs.getString("status");
                int total = rs.getInt("total");

                series.getData().add(new XYChart.Data<>(status, total));
            }

            statusChart.getData().add(series);
            for (XYChart.Data<String, Number> data : series.getData()) {

                String status = data.getXValue();

                data.nodeProperty().addListener((obs, oldNode, node) -> {
                    if(node != null){
                        switch(status){
                            case "ACCEPTE":
                                node.setStyle("-fx-bar-fill: #4CAF50;");
                                break;
                            case "REFUSE":
                                node.setStyle("-fx-bar-fill: #D9534F;");
                                break;
                            case "EN_ATTENTE":
                                node.setStyle("-fx-bar-fill: #F4B400;");
                                break;
                        }
                    }
                });
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void loadMonthlyChart(){
        try{
            monthlyChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("RDV mensuels");

            ResultSet rs = service.monthlyRdv(doctorId);

            while(rs.next()){
                int month = rs.getInt("m");
                int total = rs.getInt("total");

                String monthName = java.time.Month.of(month)
                        .getDisplayName(TextStyle.SHORT, Locale.FRANCE);

                series.getData().add(new XYChart.Data<>(monthName, total));
            }

            monthlyChart.getData().add(series);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void loadTopPatients(){
        try{
            patientFreqChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Patients fréquents");

            ResultSet rs = service.topPatients(doctorId);

            while(rs.next()){
                String name = rs.getString("full_name");
                int visits = rs.getInt("visits");

                series.getData().add(new XYChart.Data<>(name, visits));
            }

            patientFreqChart.getData().add(series);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}