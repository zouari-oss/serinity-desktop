package com.serinity.sleepcontrol.controller;

import com.serinity.sleepcontrol.model.Reve;
import com.serinity.sleepcontrol.model.Sommeil;
import com.serinity.sleepcontrol.service.ReveService;
import com.serinity.sleepcontrol.service.SommeilService;
import com.serinity.sleepcontrol.utils.PowerBIConfig;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController {

    // ══════════════════════════════════════════════
    // FXML INJECTIONS
    // ══════════════════════════════════════════════

    @FXML private Node  root;
    @FXML private VBox  revesContainer;
    @FXML private VBox  sommeilContainer;
    @FXML private Label emptyRevesLabel;
    @FXML private Label emptySommeilLabel;

    // ══════════════════════════════════════════════
    // SERVICES
    // ══════════════════════════════════════════════

    private final ReveService    reveService    = new ReveService();
    private final SommeilService sommeilService = new SommeilService();

    private static final String CSS_PATH          = "/view/styles/styles.css";
    private static final String REVE_FORM_FXML    = "/view/fxml/reve-form.fxml";
    private static final String SOMMEIL_FORM_FXML = "/view/fxml/sommeil-form.fxml";

    // ══════════════════════════════════════════════
    // INITIALIZE
    // ══════════════════════════════════════════════

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            forceCss();
            chargerReves();
            chargerSommeils();
        });
    }

    private void forceCss() {
        try {
            if (root == null || root.getScene() == null) return;
            Scene scene = root.getScene();
            var cssUrl = getClass().getResource(CSS_PATH);
            if (cssUrl == null) { System.err.println("❌ CSS introuvable: " + CSS_PATH); return; }
            String css = cssUrl.toExternalForm();
            if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
        } catch (Exception e) { e.printStackTrace(); }
    }




    private void ouvrirFormulaireSommeil(Sommeil sommeilAModifier) {
        try {
            var url = getClass().getResource("/view/fxml/sommeil-form.fxml");
            if (url == null) {
                afficherErreur("FXML introuvable", "Fichier non trouvé : /view/fxml/sommeil-form.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent parent = loader.load();

            SommeilFormController controller = loader.getController();
            controller.setSommeilService(sommeilService);
            // ✅ setSommeil appelé SEULEMENT en mode modification — exactement comme SommeilController
            if (sommeilAModifier != null) controller.setSommeil(sommeilAModifier);
            controller.setParentController(null); // ou passer un callback si besoin

            Stage stage = new Stage();
            stage.setTitle(sommeilAModifier == null ? "Ajouter une Session" : "Modifier la Session");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(parent, 550, 600));
            stage.setMinWidth(500);
            stage.setMinHeight(400);
            stage.showAndWait();
            chargerSommeils();

        } catch (Exception e) {
            afficherErreur("Erreur formulaire", e.getMessage());
            e.printStackTrace();
        }
    }

    private void ouvrirFormulaireReve(Reve reveAModifier) {
        try {
            var url = getClass().getResource("/view/fxml/reve-form.fxml");
            if (url == null) {
                afficherErreur("FXML introuvable", "Fichier non trouvé : /view/fxml/reve-form.fxml");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent parent = loader.load();

            ReveFormController controller = loader.getController();
            controller.setReveService(reveService);

            // ✅ IMPORTANT : pour charger les nuits + validation
            controller.setSommeilService(sommeilService);

            // ✅ en mode modification seulement
            if (reveAModifier != null) controller.setReve(reveAModifier);

            controller.setParentController(null);

            Stage stage = new Stage();
            stage.setTitle(reveAModifier == null ? "Ajouter un Rêve" : "Modifier le Rêve");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(parent, 550, 600));
            stage.setMinWidth(500);
            stage.setMinHeight(400);
            stage.showAndWait();

            chargerReves();

        } catch (Exception e) {
            afficherErreur("Erreur formulaire", e.getMessage());
            e.printStackTrace();
        }
    }



    // ══════════════════════════════════════════════
    // RÊVES — CHARGEMENT
    // ══════════════════════════════════════════════

    private void chargerReves() {
        revesContainer.getChildren().clear();
        try {
            List<Reve> reves = reveService.listerTous();
            if (reves == null || reves.isEmpty()) {
                emptyRevesLabel.setVisible(true);
                emptyRevesLabel.setManaged(true);
                if (!revesContainer.getChildren().contains(emptyRevesLabel))
                    revesContainer.getChildren().add(emptyRevesLabel);
                return;
            }
            emptyRevesLabel.setVisible(false);
            emptyRevesLabel.setManaged(false);
            for (Reve r : reves) revesContainer.getChildren().add(buildReveCard(r));
        } catch (SQLException e) {
            afficherErreur("Erreur BD", "Impossible de charger les rêves : " + e.getMessage());
        }
    }

    private VBox buildReveCard(Reve r) {
        VBox card = new VBox(6);
        card.getStyleClass().add("admin-item-card");
        card.setPadding(new Insets(12, 14, 12, 14));

        // ── Header : titre + badge type
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titre = new Label(r.getTitre() != null ? r.getTitre() : "Sans titre");
        titre.getStyleClass().add("card-date");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String typeReve = r.getTypeReve() != null ? r.getTypeReve() : "Normal";
        Label  badge    = new Label(capitalize(typeReve));
        badge.getStyleClass().addAll("badge", mapTypeBadge(typeReve.toLowerCase()));

        header.getChildren().addAll(titre, spacer, badge);

        // ── Infos
        Label info = new Label(
                "⚡ Intensité : " + r.getIntensite() + "/10"
                        + (r.isRecurrent() ? "  •  🔁 Récurrent" : "")
                        + (r.isCouleur()   ? "  •  🎨 Couleur"   : "")
        );
        info.getStyleClass().add("card-info");

        Label humeur = new Label(r.getHumeur() != null ? "😌 " + r.getHumeur() : "");
        humeur.getStyleClass().add("card-info-secondary");

        int   anxNiveau = r.calculerNiveauAnxiete();
        Label anxLabel  = new Label("Anxiété : " + anxNiveau + "/10");
        anxLabel.getStyleClass().add(mapAnxieteInt(anxNiveau));

        // ── 4 Boutons CRUD
        HBox footer = new HBox(6);
        footer.getStyleClass().add("card-footer");
        footer.setAlignment(Pos.CENTER_LEFT);

        // ── Dans buildReveCard et buildSommeilCard
        Button ajouter  = creerBtn("➕ Ajouter",   "btn-success");   // vert  #4CAF50
        Button modifier = creerBtn("✏ Modifier",  "btn-warning");   // orange #FF9800
        Button afficher = creerBtn("👁 Afficher",  "btn-primary");   // cyan  #26C6DA
        Button suppr    = creerBtn("🗑 Supprimer", "btn-danger");    // rouge #f44336

        ajouter.setOnAction(e  -> ouvrirFormulaireReve(null));       // mode ajout
        modifier.setOnAction(e -> ouvrirFormulaireReve(r));          // mode édition
        afficher.setOnAction(e -> voirReveDetail(r));
        suppr.setOnAction(e    -> confirmerSuppression("ce rêve", () -> {
            try { reveService.supprimer(r.getId()); chargerReves(); }
            catch (SQLException ex) { afficherErreur("Erreur", ex.getMessage()); }
        }));

        footer.getChildren().addAll(ajouter, modifier, afficher, suppr);
        card.getChildren().addAll(header, info, humeur, anxLabel, footer);
        return card;
    }

    // ══════════════════════════════════════════════
    // SOMMEIL — CHARGEMENT
    // ══════════════════════════════════════════════

    private void chargerSommeils() {
        sommeilContainer.getChildren().clear();
        try {
            List<Sommeil> sessions = sommeilService.listerTousAvecNbReves();
            if (sessions == null || sessions.isEmpty()) {
                emptySommeilLabel.setVisible(true);
                emptySommeilLabel.setManaged(true);
                if (!sommeilContainer.getChildren().contains(emptySommeilLabel))
                    sommeilContainer.getChildren().add(emptySommeilLabel);
                return;
            }
            emptySommeilLabel.setVisible(false);
            emptySommeilLabel.setManaged(false);
            for (Sommeil s : sessions) sommeilContainer.getChildren().add(buildSommeilCard(s));
        } catch (SQLException e) {
            afficherErreur("Erreur BD", "Impossible de charger les sessions : " + e.getMessage());
        }
    }

    private VBox buildSommeilCard(Sommeil s) {
        VBox card = new VBox(6);
        card.getStyleClass().add("admin-item-card");
        card.setPadding(new Insets(12, 14, 12, 14));

        // ── Header : date + badge qualité
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label date = new Label("Nuit du " + nvl(s.getDateNuit()));
        date.getStyleClass().add("card-date");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label qualite = new Label(s.getQualite() != null ? capitalize(s.getQualite()) : "—");
        qualite.getStyleClass().add(mapQualite(s.getQualite()));

        header.getChildren().addAll(date, spacer, qualite);

        // ── Infos
        Label info = new Label(
                "🕙 " + nvl(s.getHeureCoucher())
                        + " → " + nvl(s.getHeureReveil())
                        + "  •  ⏱ " + String.format("%.1f h", s.getDureeSommeil())
        );
        info.getStyleClass().add("card-info");

        Label dreams = new Label("💤 " + s.getNbReves() + " rêve(s) associé(s)");
        dreams.getStyleClass().add("card-dreams");

        int   score      = s.calculerScoreQualite();
        Label scoreLabel = new Label("Score : " + score + "/100");
        scoreLabel.getStyleClass().addAll("score-label", mapScoreLabel(score));

        // ── 4 Boutons CRUD
        HBox footer = new HBox(6);
        footer.getStyleClass().add("card-footer");
        footer.setAlignment(Pos.CENTER_LEFT);

        // ── Dans buildReveCard et buildSommeilCard
        Button ajouter  = creerBtn("➕ Ajouter",   "btn-success");   // vert  #4CAF50
        Button modifier = creerBtn("✏ Modifier",  "btn-warning");   // orange #FF9800
        Button afficher = creerBtn("👁 Afficher",  "btn-primary");   // cyan  #26C6DA
        Button suppr    = creerBtn("🗑 Supprimer", "btn-danger");    // rouge #f44336


        ajouter.setOnAction(e  -> ouvrirFormulaireSommeil(null));    // mode ajout
        modifier.setOnAction(e -> ouvrirFormulaireSommeil(s));       // mode édition
        afficher.setOnAction(e -> voirSommeilDetail(s));
        suppr.setOnAction(e    -> confirmerSuppression("cette session", () -> {
            try { sommeilService.supprimer(s.getId()); chargerSommeils(); }
            catch (SQLException ex) { afficherErreur("Erreur", ex.getMessage()); }
        }));

        footer.getChildren().addAll(ajouter, modifier, afficher, suppr);
        card.getChildren().addAll(header, info, dreams, scoreLabel, footer);
        return card;
    }

    // ══════════════════════════════════════════════
    // ACTIONS FXML BARRE DU HAUT
    // ══════════════════════════════════════════════

    @FXML private void ajouterReve()        { ouvrirFormulaireReve(null); }
    @FXML private void modifierReve()       { afficherInfo("Info", "Cliquez ✏ sur la card à modifier."); }
    @FXML private void supprimerReve()      { afficherInfo("Info", "Cliquez 🗑 sur la card à supprimer."); }
    @FXML private void actualiserReves()    { chargerReves(); }

    @FXML private void ajouterSommeil()     { ouvrirFormulaireSommeil(null); }
    @FXML private void modifierSommeil()    { afficherInfo("Info", "Cliquez ✏ sur la card à modifier."); }
    @FXML private void supprimerSommeil()   { afficherInfo("Info", "Cliquez 🗑 sur la card à supprimer."); }
    @FXML private void actualiserSommeils() { chargerSommeils(); }

    @FXML private void gererSommeil() { ouvrirLien(PowerBIConfig.ADMIN_SOMMEIL_MANAGE_URL, "Statistiques Sommeil"); }
    @FXML private void gererReve()    { ouvrirLien(PowerBIConfig.ADMIN_REVE_MANAGE_URL,    "Statistiques Rêve"); }

    @FXML private void retour() {
        try {
            if (root != null && root.getScene() != null)
                ((Stage) root.getScene().getWindow()).close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ══════════════════════════════════════════════
    // AFFICHAGE DÉTAILS
    // ══════════════════════════════════════════════

    private void voirReveDetail(Reve r) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Détail du Rêve");
        a.setHeaderText(r.getTitre());
        a.setContentText(
                "🏷 Type       : " + nvl(r.getTypeReve())               + "\n" +
                        "⚡ Intensité  : " + r.getIntensite()   + "/10"         + "\n" +
                        "😰 Anxiété    : " + r.calculerNiveauAnxiete() + "/10"  + "\n" +
                        "😌 Humeur     : " + nvl(r.getHumeur())                 + "\n" +
                        "😊 Émotions   : " + nvl(r.getEmotions())               + "\n" +
                        "🔣 Symboles   : " + nvl(r.getSymboles())               + "\n" +
                        "🔁 Récurrent  : " + (r.isRecurrent() ? "Oui" : "Non") + "\n" +
                        "🎨 Couleur    : " + (r.isCouleur()   ? "Oui" : "Non") + "\n\n" +
                        nvl(r.getDescription())
        );
        a.showAndWait();
    }

    private void voirSommeilDetail(Sommeil s) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Détail de la Session");
        a.setHeaderText("Nuit du " + nvl(s.getDateNuit()));
        a.setContentText(
                "🕙 Coucher       : " + nvl(s.getHeureCoucher())                    + "\n" +
                        "⏰ Réveil        : " + nvl(s.getHeureReveil())                     + "\n" +
                        "⏱ Durée         : " + String.format("%.1f h", s.getDureeSommeil()) + "\n" +
                        "🌟 Qualité       : " + nvl(s.getQualite())                         + "\n" +
                        "📊 Score         : " + s.calculerScoreQualite() + "/100"           + "\n" +
                        "😌 Humeur réveil : " + nvl(s.getHumeurReveil())                    + "\n" +
                        "💤 Rêves liés    : " + s.getNbReves()                              + "\n" +
                        "🏠 Environnement : " + nvl(s.getEnvironnement())
        );
        a.showAndWait();
    }

    // ══════════════════════════════════════════════
    // POWER BI
    // ══════════════════════════════════════════════

    private void ouvrirLien(String url, String nom) {
        try {
            if (url == null || url.isBlank() || url.contains("COLLE_ICI")) {
                afficherErreur("Lien manquant", "Lien Power BI non configuré : " + nom); return;
            }
            if (!Desktop.isDesktopSupported()) {
                afficherErreur("Erreur", "Navigateur non supporté."); return;
            }
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            afficherErreur("Erreur", e.getMessage()); e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════
    // MAPPERS CSS
    // ══════════════════════════════════════════════

    private String mapTypeBadge(String type) {
        return switch (type) {
            case "cauchemar"             -> "badge-cauchemar";
            case "lucide"                -> "badge-lucide";
            case "récurrent","recurrent" -> "badge-recurrent";
            default                      -> "badge-normal";
        };
    }

    private String mapQualite(String q) {
        if (q == null) return "qualite-moyenne";
        return switch (q.toLowerCase()) {
            case "excellente" -> "qualite-excellente";
            case "bonne"      -> "qualite-bonne";
            case "moyenne"    -> "qualite-moyenne";
            default           -> "qualite-mauvaise";
        };
    }

    private String mapScoreLabel(int score) {
        if (score >= 80) return "score-excellent";
        if (score >= 50) return "score-moyen";
        return "score-faible";
    }

    private String mapAnxieteInt(int niveau) {
        if (niveau >= 7) return "anxiete-elevee";
        if (niveau >= 4) return "anxiete-moyenne";
        return "anxiete-faible";
    }

    // ══════════════════════════════════════════════
    // UTILITAIRES
    // ══════════════════════════════════════════════

    private Button creerBtn(String texte, String styleClass) {
        Button btn = new Button(texte);
        btn.getStyleClass().add(styleClass);
        return btn;
    }

    private void confirmerSuppression(String cible, Runnable action) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer " + cible + " ?");
        confirm.setContentText("Cette action est irréversible.");
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) action.run();
    }

    private void afficherInfo(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void afficherErreur(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titre); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private String nvl(Object o) { return o != null ? o.toString() : "—"; }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
