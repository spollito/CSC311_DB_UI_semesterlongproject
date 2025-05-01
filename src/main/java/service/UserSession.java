package service;

import java.util.prefs.Preferences;

public class UserSession {

    private static volatile UserSession instance;

    private String userName;
    private String password;
    private String privileges;

    private static final String PREF_USERNAME = "USERNAME";
    private static final String PREF_PASSWORD = "PASSWORD";
    private static final String PREF_PRIVILEGES = "PRIVILEGES";

    private static final Preferences prefs = Preferences.userRoot().node("UserSession");

    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;

        prefs.put(PREF_USERNAME, userName);
        prefs.put(PREF_PASSWORD, password);
        prefs.put(PREF_PRIVILEGES, privileges);
    }

    public static UserSession signUp(String userName, String password, String privileges) {
        synchronized (UserSession.class) {
            if (prefs.get(PREF_USERNAME, null) != null) {
                throw new IllegalStateException("User already signed up.");
            }
            instance = new UserSession(userName, password, privileges);
            return instance;
        }
    }

    public static UserSession signIn(String userName, String password) {
        synchronized (UserSession.class) {
            String storedUser = prefs.get(PREF_USERNAME, null);
            String storedPass = prefs.get(PREF_PASSWORD, null);

            if (storedUser != null && storedUser.equals(userName) && storedPass.equals(password)) {
                if (instance == null) {
                    instance = new UserSession(userName, password, prefs.get(PREF_PRIVILEGES, "NONE"));
                }
                return instance;
            } else {
                throw new SecurityException("Invalid credentials.");
            }
        }
    }

    public static boolean isUserRegistered() {
        return prefs.get(PREF_USERNAME, null) != null;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getPrivileges() {
        return privileges;
    }

    public void cleanUserSession() {
        this.userName = "";
        this.password = "";
        this.privileges = "";
        prefs.remove(PREF_USERNAME);
        prefs.remove(PREF_PASSWORD);
        prefs.remove(PREF_PRIVILEGES);
        instance = null;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userName='" + userName + '\'' +
                ", privileges='" + privileges + '\'' +
                '}';
    }
}
