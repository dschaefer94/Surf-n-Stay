package model;


public class User {

    private int id;
    private String username;
    private String displayName;

    public User(){
        this.id=0;
        this.username="";
        this.displayName="";
    }

    public User(String username, String displayName) {
        this.id = 0;
        this.username= username;
        this.displayName = getDisplayName();
    }

    public User(int id, String username, String displayName) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
