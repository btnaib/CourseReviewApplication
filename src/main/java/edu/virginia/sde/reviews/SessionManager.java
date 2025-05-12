package edu.virginia.sde.reviews;

public class SessionManager {
    private static int userId;

    public static void setUserId(int id) {
        userId = id;
    }

    public static int getUserId() {
        return userId;
    }
}
