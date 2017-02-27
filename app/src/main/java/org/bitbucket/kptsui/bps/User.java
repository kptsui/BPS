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
    private String id;

    private User() {

    }

    public void save(String id, String name, String pw){
        this.id = id;
        this.name = name;
        this.pw = pw;
        App.getInstance().getSharedPreferences()
                .edit()
                .putString(App.PREFS_USER_ID_KEY, id)
                .putString(App.PREFS_USER_NAME_KEY, name)
                .putString(App.PREFS_USER_PW_KEY, pw)
                .apply();
    }

    public void clean(){
        this.id = null;
        this.name = null;
        this.pw = null;
        App.getInstance().getSharedPreferences()
                .edit()
                .putString(App.PREFS_USER_ID_KEY, id)
                .putString(App.PREFS_USER_NAME_KEY, name)
                .putString(App.PREFS_USER_PW_KEY, pw)
                .apply();
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;

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
        return id != null
                && name != null
                && pw != null
                && !id.isEmpty()
                && !name.isEmpty()
                && !pw.isEmpty();
    }
}
