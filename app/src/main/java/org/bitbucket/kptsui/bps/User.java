package org.bitbucket.kptsui.bps;

/**
 * Created by user on 21/2/2017.
 */
public class User {
    private static User ourInstance = new User();

    public static User getInstance() {
        return ourInstance;
    }

    private String name;
    private String pw;

    private User() {

    }

    public void save(String name, String pw){
        this.name = name;
        this.pw = pw;
        App.getInstance().getSharedPreferences()
                .edit()
                .putString(App.PREFS_USER_NAME_KEY, name)
                .putString(App.PREFS_USER_PW_KEY, pw)
                .apply();
    }

    public void clean(){
        this.name = null;
        this.pw = null;
        App.getInstance().getSharedPreferences()
                .edit()
                .putString(App.PREFS_USER_NAME_KEY, name)
                .putString(App.PREFS_USER_PW_KEY, pw)
                .apply();
    }

    public String getName() {
        return name;
    }

    public String getPw() {
        return pw;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public boolean isLogged(){
        return name != null && pw != null & !name.isEmpty() && !pw.isEmpty();
    }
}
