package com.absensi.inuraini.admin.rekap;

public class DataRekap {
    String sAlamat;
    String sEmail;
    String sGender;
    String sJabatan;
    String sNama;
    String sPhone;
    String sStatus;
    String sTtl;
    boolean sVerified;

    public DataRekap(String sAlamat, String sEmail, String sGender, String sJabatan, String sNama, String sPhone, String sStatus, String sTtl, boolean sVerified) {
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

    public DataRekap() {
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
