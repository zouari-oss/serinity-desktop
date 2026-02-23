package esprit.tn.oussema_javafx.utils;

public class AppSession {

    // id du patient "connecté" (tu as demandé toujours 1)
    private static int currentUserId = 1;

    // navigation / sélection
    private static int selectedRdvId;
    private static int selectedDoctorId;

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int id) {
        currentUserId = id;
    }

    public static int getSelectedRdvId() {
        return selectedRdvId;
    }

    public static void setSelectedRdvId(int id) {
        selectedRdvId = id;
    }

    public static int getSelectedDoctorId() {
        return selectedDoctorId;
    }

    public static void setSelectedDoctorId(int id) {
        selectedDoctorId = id;
    }

    public static void clearSelection() {
        selectedRdvId = 0;
        selectedDoctorId = 0;
    }
}
