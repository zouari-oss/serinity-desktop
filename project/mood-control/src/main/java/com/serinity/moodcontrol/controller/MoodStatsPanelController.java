package com.serinity.moodcontrol.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Slide-in stats panel controller.
 * Data source: cached MoodHistory items passed from MoodHistoryController (no DB hits).
 */
public class MoodStatsPanelController {

    private static final int LAST_DAYS = 30;
    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("MM-dd");

    private static final String[] LEVEL_COLORS = new String[] {
            "",             // 0 unused
            "#D9534F",       // 1 very low
            "#F0AD4E",       // 2 low
            "#9E9E9E",       // 3 neutral
            "#5BC0DE",       // 4 good
            "#5CB85C"        // 5 very good
    };

    @FXML private Button btnClose;

    @FXML private ToggleGroup tabGroup;
    @FXML private ToggleButton tabMoment;
    @FXML private ToggleButton tabDay;
    @FXML private ToggleButton tabTotal;

    @FXML private PieChart pieChart;

    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis barXAxis;
    @FXML private NumberAxis barYAxis;

    @FXML private LineChart<String, Number> lineChart;
    @FXML private CategoryAxis lineXAxis;
    @FXML private NumberAxis lineYAxis;

    @FXML private Label lblNoData;

    private Runnable onClose;

    private List<MoodHistoryController.MoodCardVM> items =
            new ArrayList<MoodHistoryController.MoodCardVM>();

    @FXML
    public void initialize() {
        showMoment();
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void setItems(List<MoodHistoryController.MoodCardVM> items) {
        this.items = (items == null) ? new ArrayList<MoodHistoryController.MoodCardVM>() : items;
        rebuildAll();
    }

    @FXML
    private void onClose() {
        if (onClose != null) onClose.run();
    }

    @FXML
    private void onSelectMoment() {
        showMoment();
    }

    @FXML
    private void onSelectDay() {
        showDay();
    }

    @FXML
    private void onSelectTotal() {
        showTotal();
    }

    // ------------------ View switching ------------------

    private void showMoment() {
        setVisible(pieChart, true);
        setVisible(barChart, false);
        setVisible(lineChart, false);
    }

    private void showDay() {
        setVisible(pieChart, false);
        setVisible(barChart, true);
        setVisible(lineChart, false);
    }

    private void showTotal() {
        setVisible(pieChart, false);
        setVisible(barChart, false);
        setVisible(lineChart, true);
    }

    private void setVisible(Chart chart, boolean v) {
        chart.setVisible(v);
        chart.setManaged(v);
    }

    // ------------------ Data build ------------------

    private void rebuildAll() {
        buildPie();
        buildBar();
        buildLine();

        boolean empty = items == null || items.isEmpty();
        lblNoData.setVisible(empty);
        lblNoData.setManaged(empty);
    }

    private List<MoodHistoryController.MoodCardVM> last30Days(List<MoodHistoryController.MoodCardVM> src) {
        if (src == null) return new ArrayList<MoodHistoryController.MoodCardVM>();
        LocalDateTime cutoff = LocalDate.now().minusDays(LAST_DAYS).atStartOfDay();

        List<MoodHistoryController.MoodCardVM> out = new ArrayList<MoodHistoryController.MoodCardVM>();
        for (MoodHistoryController.MoodCardVM m : src) {
            if (m == null || m.dateTime == null) continue;
            if (!m.dateTime.isBefore(cutoff)) out.add(m);
        }
        return out;
    }

    private void buildPie() {
        pieChart.getData().clear();

        List<MoodHistoryController.MoodCardVM> src = last30Days(items);

        int[] counts = new int[6];

        for (MoodHistoryController.MoodCardVM m : src) {
            if (m == null) continue;

            String mt = safe(m.momentType).toUpperCase(Locale.ROOT);
            if (!"MOMENT".equals(mt)) continue;

            int lvl = m.moodLevel;
            if (lvl < 1 || lvl > 5) continue;
            counts[lvl]++;

        }
        //lvls debug
        //System.out.println("Invalid levels: " + src.stream().filter(m -> m.moodLevel < 1 || m.moodLevel > 5).count());
        boolean hasAny = false;
        for (int lvl = 1; lvl <= 5; lvl++) {
            if (counts[lvl] <= 0) continue;
            hasAny = true;
            pieChart.getData().add(new PieChart.Data(levelLabel(lvl), counts[lvl]));
        }

        Platform.runLater(this::applyPieColors);

        lblNoData.setVisible(!hasAny);
        lblNoData.setManaged(!hasAny);
    }

    private void applyPieColors() {
        for (PieChart.Data d : pieChart.getData()) {
            int lvl = parseLevelFromLabel(d.getName());
            String color = colorForLevel(lvl);
            if (d.getNode() != null && color != null) {
                d.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
        }
    }

    private void buildBar() {
        barChart.getData().clear();

        List<MoodHistoryController.MoodCardVM> src = last30Days(items);

        // Group by date -> sum, count
        Map<LocalDate, SumCount> byDay = new TreeMap<LocalDate, SumCount>();
        for (MoodHistoryController.MoodCardVM m : src) {
            if (m == null || m.dateTime == null) continue;
            LocalDate d = m.dateTime.toLocalDate();
            SumCount sc = byDay.get(d);
            if (sc == null) {
                sc = new SumCount();
                byDay.put(d, sc);
            }
            sc.sum += clampLevel(m.moodLevel);
            sc.count += 1;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        series.setName("Avg mood");

        for (Map.Entry<LocalDate, SumCount> e : byDay.entrySet()) {
            LocalDate date = e.getKey();
            SumCount sc = e.getValue();
            double avg = sc.count == 0 ? 0.0 : (sc.sum / (double) sc.count);

            String x = DATE_LABEL.format(date);
            XYChart.Data<String, Number> data = new XYChart.Data<String, Number>(x, avg);
            series.getData().add(data);

            final int cnt = sc.count;
            final double avgFinal = avg;
            Platform.runLater(() -> {
                if (data.getNode() != null) {
                    Tooltip.install(data.getNode(),
                            new Tooltip(String.format("%s\nAvg: %.2f\nEntries: %d", date, avgFinal, cnt)));

                    // Color by rounded average mood level
                    int lvl = clampLevel((int) Math.round(avgFinal));
                    String color = colorForLevel(lvl);
                    if (color != null) {
                        data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                    }
                }
            });
        }

        barChart.getData().add(series);
    }

    private void buildLine() {
        lineChart.getData().clear();

        List<MoodHistoryController.MoodCardVM> src = last30Days(items);

        Map<LocalDate, SumCount> dayType = new TreeMap<LocalDate, SumCount>();
        Map<LocalDate, SumCount> momentType = new TreeMap<LocalDate, SumCount>();

        for (MoodHistoryController.MoodCardVM m : src) {
            if (m == null || m.dateTime == null) continue;

            LocalDate d = m.dateTime.toLocalDate();
            String mt = safe(m.momentType).toUpperCase(Locale.ROOT);

            if ("DAY".equals(mt)) {
                SumCount sc = dayType.get(d);
                if (sc == null) { sc = new SumCount(); dayType.put(d, sc); }
                sc.sum += clampLevel(m.moodLevel);
                sc.count++;
            } else if ("MOMENT".equals(mt)) {
                SumCount sc = momentType.get(d);
                if (sc == null) { sc = new SumCount(); momentType.put(d, sc); }
                sc.sum += clampLevel(m.moodLevel);
                sc.count++;
            }
        }

        XYChart.Series<String, Number> sDay = new XYChart.Series<String, Number>();
        sDay.setName("DAY entries");

        XYChart.Series<String, Number> sMoment = new XYChart.Series<String, Number>();
        sMoment.setName("MOMENT entries");

        for (Map.Entry<LocalDate, SumCount> e : dayType.entrySet()) {
            LocalDate date = e.getKey();
            SumCount sc = e.getValue();
            double avg = sc.count == 0 ? 0.0 : (sc.sum / (double) sc.count);
            sDay.getData().add(new XYChart.Data<String, Number>(DATE_LABEL.format(date), avg));
        }

        for (Map.Entry<LocalDate, SumCount> e : momentType.entrySet()) {
            LocalDate date = e.getKey();
            SumCount sc = e.getValue();
            double avg = sc.count == 0 ? 0.0 : (sc.sum / (double) sc.count);
            sMoment.getData().add(new XYChart.Data<String, Number>(DATE_LABEL.format(date), avg));
        }

        lineChart.getData().add(sDay);
        lineChart.getData().add(sMoment);
    }

    //Helpers

    private static class SumCount {
        double sum = 0.0;
        int count = 0;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private int clampLevel(int lvl) {
        if (lvl < 1) return 1;
        if (lvl > 5) return 5;
        return lvl;
    }

    private String levelLabel(int lvl) {
        switch (lvl) {
            case 1: return "Very low";
            case 2: return "Low";
            case 3: return "Neutral";
            case 4: return "Good";
            case 5: return "Very good";
            default: return String.valueOf(lvl);
        }
    }

    private int parseLevelFromLabel(String name) {
        if (name == null) return 3;
        String n = name.toLowerCase(Locale.ROOT);
        if (n.contains("very low")) return 1;
        if (n.equals("low")) return 2;
        if (n.contains("neutral")) return 3;
        if (n.equals("good")) return 4;
        if (n.contains("very good")) return 5;
        return 3;
    }

    private String colorForLevel(int lvl) {
        if (lvl < 1 || lvl > 5) return null;
        return LEVEL_COLORS[lvl];
    }
}