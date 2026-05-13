package com.serinity.consultationcontrol.util;

public class AppSession {

    // id du patient "connecté" (tu as demandé toujours 1)
    private static String currentUserId;

    // navigation / sélection
    private static int selectedRdvId;
    private static String selectedDoctorId;

    public static String getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(String id) {
        currentUserId = id;
    }

    public static int getSelectedRdvId() {
        return selectedRdvId;
    }

    public static void setSelectedRdvId(int id) {
        selectedRdvId = id;
    }

    public static String getSelectedDoctorId() {
        return selectedDoctorId;
    }

    public static void setSelectedDoctorId(String id) {
        selectedDoctorId = id;
    }

    public static void clearSelection() {
        selectedRdvId = 0;
        selectedDoctorId = null;
    }
}
