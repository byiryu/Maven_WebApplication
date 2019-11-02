package kr.byiryu.db;

public enum DBInfo {
    TYPE("jdbc"),
    TOOLS("mysql"),
    DOMAIN("127.0.0.1"),
    PORT("3306"),
    ID("admin"),
    PW("admin"),
    DATABASE("byiryu");

    private String title;

    DBInfo(String title){
        this.title = title;
    }
    public String toString(){
        return this.title;
    }
}
