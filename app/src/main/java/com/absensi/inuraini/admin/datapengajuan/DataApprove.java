package com.absensi.inuraini.admin.datapengajuan;

import java.util.Comparator;

public class DataApprove {
    String key, sKet;
    boolean sAcc, sKonfirmAdmin;

    public DataApprove(String sKet, boolean sAcc, boolean sKonfirmAdmin) {
        this.sKet = sKet;
        this.sAcc = sAcc;
        this.sKonfirmAdmin = sKonfirmAdmin;
    }

    public DataApprove() {
    }

    public static Comparator<DataApprove> dataApproveComparator = (dataApprove, t1) -> {
        if (dataApprove.getKey() != null) {
            return -dataApprove.getKey().compareTo(t1.getKey());
        }
        return 0;
    };

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
