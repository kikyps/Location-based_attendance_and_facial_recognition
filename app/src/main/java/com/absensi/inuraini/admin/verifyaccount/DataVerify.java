package com.absensi.inuraini.admin.verifyaccount;

import java.util.Comparator;

public class DataVerify {
    String key;
    String sAlamat;
    String sEmail;
    String sGender;
    String sJabatan;
    String sNama;
    String sPhone;
    String sStatus;
    String sTtl;
    boolean sVerified;

    public DataVerify(String key, String sAlamat, String sEmail, String sGender, String sJabatan, String sNama, String sPhone, String sStatus, String sTtl, boolean sVerified) {
        this.key = key;
        this.sAlamat = sAlamat;
        this.sEmail = sEmail;
        this.sGender = sGender;
        this.sJabatan = sJabatan;
        this.sNama = sNama;
        this.sPhone = sPhone;
        this.sStatus = sStatus;
        this.sTtl = sTtl;
        this.sVerified = sVerified;
    }

    public DataVerify() {
    }

    public static Comparator<DataVerify> dataVerifyComparator = new Comparator<DataVerify>() {
        @Override
        public int compare(DataVerify dataVerify, DataVerify t1) {
            if (dataVerify.getsNama() != null) {
                return dataVerify.getsNama().compareTo(t1.getsNama());
            }
            return 0;
        }
    };

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getsAlamat() {
        return sAlamat;
    }

    public void setsAlamat(String sAlamat) {
        this.sAlamat = sAlamat;
    }

    public String getsEmail() {
        return sEmail;
    }

    public void setsEmail(String sEmail) {
        this.sEmail = sEmail;
    }

    public String getsGender() {
        return sGender;
    }

    public void setsGender(String sGender) {
        this.sGender = sGender;
    }

    public String getsJabatan() {
        return sJabatan;
    }

    public void setsJabatan(String sJabatan) {
        this.sJabatan = sJabatan;
    }

    public String getsNama() {
        return sNama;
    }

    public void setsNama(String sNama) {
        this.sNama = sNama;
    }

    public String getsPhone() {
        return sPhone;
    }

    public void setsPhone(String sPhone) {
        this.sPhone = sPhone;
    }

    public String getsStatus() {
        return sStatus;
    }

    public void setsStatus(String sStatus) {
        this.sStatus = sStatus;
    }

    public String getsTtl() {
        return sTtl;
    }

    public void setsTtl(String sTtl) {
        this.sTtl = sTtl;
    }

    public boolean issVerified() {
        return sVerified;
    }

    public void setsVerified(boolean sVerified) {
        this.sVerified = sVerified;
    }
}
