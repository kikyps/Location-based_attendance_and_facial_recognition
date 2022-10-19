package com.absensi.inuraini.admin.datapengajuan;

public class DataFilterIzin {
    String key, sKet;
    boolean sAcc, sKonfirmAdmin;

    public DataFilterIzin(String sKet, boolean sAcc, boolean sKonfirmAdmin) {
        this.sKet = sKet;
        this.sAcc = sAcc;
        this.sKonfirmAdmin = sKonfirmAdmin;
    }

    public DataFilterIzin() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getsKet() {
        return sKet;
    }

    public void setsKet(String sKet) {
        this.sKet = sKet;
    }

    public boolean issAcc() {
        return sAcc;
    }

    public void setsAcc(boolean sAcc) {
        this.sAcc = sAcc;
    }

    public boolean issKonfirmAdmin() {
        return sKonfirmAdmin;
    }

    public void setsKonfirmAdmin(boolean sKonfirmAdmin) {
        this.sKonfirmAdmin = sKonfirmAdmin;
    }
}
