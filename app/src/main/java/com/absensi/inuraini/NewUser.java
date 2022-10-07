package com.absensi.inuraini;

public class NewUser {
    String sUser;
    boolean sVerified;

    public NewUser(String sUser, boolean sVerified) {
        this.sUser = sUser;
        this.sVerified = sVerified;
    }

    public NewUser() {
    }

    public String getsUser() {
        return sUser;
    }

    public void setsUser(String sUser) {
        this.sUser = sUser;
    }

    public boolean issVerified() {
        return sVerified;
    }

    public void setsVerified(boolean sVerified) {
        this.sVerified = sVerified;
    }
}
