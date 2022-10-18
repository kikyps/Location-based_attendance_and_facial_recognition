package com.absensi.inuraini.user.pengajuan;

import com.absensi.inuraini.admin.verifyaccount.DataVerify;

import java.util.Comparator;

public class DataIzin {
    String key, sKet;
    boolean sAcc, sKonfirmAdmin;

    public DataIzin(String sKet, boolean sAcc, boolean sKonfirmAdmin) {
        this.sKet = sKet;
        this.sAcc = sAcc;
        this.sKonfirmAdmin = sKonfirmAdmin;
    }

    public static Comparator<DataIzin> dataIzinComparator = (dataVerify, t1) -> {
        if (dataVerify.getKey() != null) {
            return -dataVerify.getKey().compareTo(t1.getKey());
        }
        return 0;
    };

    public DataIzin() {
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
