package com.serinity.forumcontrol.Controllers;

import com.serinity.forumcontrol.Models.ForumStatistics;
import com.serinity.forumcontrol.Services.ServiceStatistics;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller for Statistics View with Charts (Admin only)
 */
public class StatisticsController {

    // Labels for overview cards
    @FXML private Label totalThreadsLabel;
    @FXML private Label totalRepliesLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalCategoriesLabel;
    @FXML private Label activeTodayLabel;
    @FXML private Label activeWeekLabel;
    @FXML private Label totalLikesLabel;
    @FXML private Label totalDislikesLabel;
    @FXML private Label totalFollowsLabel;

    // Charts
    @FXML private PieChart statusPieChart;
    @FXML private PieChart typePieChart;
    @FXML private LineChart<String, Number> threadsLineChart;
    @FXML private BarChart<String, Number> topUsersBarChart;
    @FXML private CategoryAxis dateAxis;
    @FXML private NumberAxis countAxis;
    @FXML private CategoryAxis usernameAxis;
    @FXML private NumberAxis activityAxis;

    private ServiceStatistics statisticsService;

    public StatisticsController() {
        this.statisticsService = new ServiceStatistics();
    }

    @FXML
    public void initialize() {
        loadStatistics();
    }

    /**
     * Load and display all statistics
     */
    private void loadStatistics() {
        ForumStatistics stats = statisticsService.getForumStatistics();

        // Update main counters
        totalThreadsLabel.setText(String.valueOf(stats.getTotalThreads()));
        totalRepliesLabel.setText(String.valueOf(stats.getTotalReplies()));
        totalUsersLabel.setText(String.valueOf(stats.getTotalUsers()));
        totalCategoriesLabel.setText(String.valueOf(stats.getTotalCategories()));

        // Update user activity
        activeTodayLabel.setText(stats.getActiveUsersToday() + " users");
        activeWeekLabel.setText(stats.getActiveUsersThisWeek() + " users");

        // Update interactions
        totalLikesLabel.setText(String.valueOf(stats.getTotalLikes()));
        totalDislikesLabel.setText(String.valueOf(stats.getTotalDislikes()));
        totalFollowsLabel.setText(String.valueOf(stats.getTotalFollows()));

        // Load charts
        loadStatusPieChart(stats);
        loadTypePieChart(stats);
        loadThreadsLineChart();
        loadTopUsersBarChart();
    }

    /**
     * Load Thread Status Pie Chart
     */
    private void loadStatusPieChart(ForumStatistics stats) {
        statusPieChart.getData().clear();

        if (stats.getTotalThreads() > 0) {
            PieChart.Data openData = new PieChart.Data(
                    "Open (" + stats.getOpenThreads() + ")",
                    stats.getOpenThreads()
            );

            PieChart.Data lockedData = new PieChart.Data(
                    "Locked (" + stats.getLockedThreads() + ")",
                    stats.getLockedThreads()
            );

            PieChart.Data archivedData = new PieChart.Data(
                    "Archived (" + stats.getArchivedThreads() + ")",
                    stats.getArchivedThreads()
            );

            statusPieChart.getData().addAll(openData, lockedData, archivedData);

            // Apply colors after data is added
            statusPieChart.applyCss();
            statusPieChart.layout();

            // Set colors for each slice
            if (openData.getNode() != null) {
                openData.getNode().setStyle("-fx-pie-color: #4CAF50;");
            }
            if (lockedData.getNode() != null) {
                lockedData.getNode().setStyle("-fx-pie-color: #FF9800;");
            }
            if (archivedData.getNode() != null) {
                archivedData.getNode().setStyle("-fx-pie-color: #9E9E9E;");
            }
        }
    }

    /**
     * Load Thread Type Pie Chart
     */
    private void loadTypePieChart(ForumStatistics stats) {
        typePieChart.getData().clear();

        if (stats.getTotalThreads() > 0) {
            PieChart.Data discussionData = new PieChart.Data(
                    "Discussion (" + stats.getDiscussionThreads() + ")",
                    stats.getDiscussionThreads()
            );

            PieChart.Data questionData = new PieChart.Data(
                    "Question (" + stats.getQuestionThreads() + ")",
                    stats.getQuestionThreads()
            );

            PieChart.Data announcementData = new PieChart.Data(
                    "Announcement (" + stats.getAnnouncementThreads() + ")",
                    stats.getAnnouncementThreads()
            );

            typePieChart.getData().addAll(discussionData, questionData, announcementData);

            // Apply colors
            typePieChart.applyCss();
            typePieChart.layout();

            if (discussionData.getNode() != null) {
                discussionData.getNode().setStyle("-fx-pie-color: #2196F3;");
            }
            if (questionData.getNode() != null) {
                questionData.getNode().setStyle("-fx-pie-color: #FF5722;");
            }
            if (announcementData.getNode() != null) {
                announcementData.getNode().setStyle("-fx-pie-color: #9C27B0;");
            }
        }
    }

    /**
     * Load Threads Over Time Line Chart
     */
    private void loadThreadsLineChart() {
        threadsLineChart.getData().clear();

        Map<String, Integer> threadsPerDay = statisticsService.getThreadsPerDay();

        if (!threadsPerDay.isEmpty()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Threads");

            // Sort by date
            List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(threadsPerDay.entrySet());
            sortedEntries.sort(Map.Entry.comparingByKey());

            // Add data points (showing last 15 days for readability)
            int dataPoints = Math.min(15, sortedEntries.size());
            for (int i = sortedEntries.size() - dataPoints; i < sortedEntries.size(); i++) {
                Map.Entry<String, Integer> entry = sortedEntries.get(i);

                // Format date for display (MM/dd)
                try {
                    LocalDate date = LocalDate.parse(entry.getKey());
                    String formattedDate = date.format(DateTimeFormatter.ofPattern("MM/dd"));
                    series.getData().add(new XYChart.Data<>(formattedDate, entry.getValue()));
                } catch (Exception e) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
            }

            threadsLineChart.getData().add(series);

            // Style the line
            threadsLineChart.applyCss();
            threadsLineChart.layout();
        }
    }

    /**
     * Load Top Users Bar Chart
     */
    private void loadTopUsersBarChart() {
        topUsersBarChart.getData().clear();

        Map<String, Integer> topUsers = statisticsService.getTopActiveUsers();

        if (!topUsers.isEmpty()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Activity");

            // Sort by activity count (descending)
            List<Map.Entry<String, Integer>> sortedUsers = new ArrayList<>(topUsers.entrySet());
            sortedUsers.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            // Add top 10 users
            int count = 0;
            for (Map.Entry<String, Integer> entry : sortedUsers) {
                if (count >= 10) break;

                String username = entry.getKey();
                // Truncate long usernames
                if (username.length() > 12) {
                    username = username.substring(0, 10) + "...";
                }

                series.getData().add(new XYChart.Data<>(username, entry.getValue()));
                count++;
            }

            topUsersBarChart.getData().add(series);

            // Apply styling
            topUsersBarChart.applyCss();
            topUsersBarChart.layout();

            // Color bars with gradient
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> data = series.getData().get(i);
                if (data.getNode() != null) {
                    // Gold for #1, Silver for #2, Bronze for #3, Blue for rest
                    String color = switch (i) {
                        case 0 -> "#FFD700"; // Gold
                        case 1 -> "#C0C0C0"; // Silver
                        case 2 -> "#CD7F32"; // Bronze
                        default -> "#2196F3"; // Blue
                    };
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        }
    }

    /**
     * Refresh statistics
     */
    @FXML
    private void onRefresh() {
        loadStatistics();
        System.out.println("Statistics refreshed!");
    }

    /**
     * Go back to forum posts view
     */
    @FXML
    private void onBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/forum/ForumBackoffice.fxml")
            );
            Parent forumView = loader.load();

            BorderPane root = findBorderPane();
            if (root != null) {
                root.setCenter(forumView);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading forum view: " + e.getMessage());
        }
    }

    /**
     * Find the root BorderPane
     */
    private BorderPane findBorderPane() {
        if (totalThreadsLabel != null && totalThreadsLabel.getScene() != null) {
            javafx.scene.Node node = totalThreadsLabel.getScene().getRoot();
            if (node instanceof BorderPane) {
                return (BorderPane) node;
            }
            while (node != null) {
                if (node instanceof BorderPane) {
                    return (BorderPane) node;
                }
                node = node.getParent();
            }
        }
        return null;
    }
}