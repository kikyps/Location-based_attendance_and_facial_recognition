package com.absensi.inuraini.user.absen;

public class AbsenData {
    String key, sJamMasuk, sJamKeluar, sKet, sLokasi;
    boolean sKantor, sKehadiran, sTerlambat, sLembur;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getsJamMasuk() {
        return sJamMasuk;
    }

    public void setsJamMasuk(String sJamMasuk) {
        this.sJamMasuk = sJamMasuk;
    }

    public String getsJamKeluar() {
        return sJamKeluar;
    }

    public void setsJamKeluar(String sJamKeluar) {
        this.sJamKeluar = sJamKeluar;
    }

    public String getsKet() {
        return sKet;
    }

    public void setsKet(String sKet) {
        this.sKet = sKet;
    }

    public String getsLokasi() {
        return sLokasi;
    }

    public void setsLokasi(String sLokasi) {
        this.sLokasi = sLokasi;
    }

    public boolean issKantor() {
        return sKantor;
    }

    public void setsKantor(boolean sKantor) {
        this.sKantor = sKantor;
    }

    public boolean issKehadiran() {
        return sKehadiran;
    }

    public void setsKehadiran(boolean sKehadiran) {
        this.sKehadiran = sKehadiran;
    }

    public boolean issTerlambat() {
        return sTerlambat;
    }

    public void setsTerlambat(boolean sTerlambat) {
        this.sTerlambat = sTerlambat;
    }

    public boolean issLembur() {
        return sLembur;
    }

    public void setsLembur(boolean sLembur) {
        this.sLembur = sLembur;
    }

    public AbsenData(String sJamMasuk, String sJamKeluar, String sKet, String sLokasi, boolean sKantor, boolean sKehadiran, boolean sTerlambat, boolean sLembur) {
        this.sJamMasuk = sJamMasuk;
        this.sJamKeluar = sJamKeluar;
        this.sKet = sKet;
        this.sLokasi = sLokasi;
        this.sKantor = sKantor;
        this.sKehadiran = sKehadiran;
        this.sTerlambat = sTerlambat;
        this.sLembur = sLembur;
    }

    public AbsenData() {
    }
}
