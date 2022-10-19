package com.absensi.inuraini.admin.datapengajuan;

public class DataReqIzin {
    String key;
    String sNama;
    String sStatus;
    boolean sKonfirmAdmin, sKehadiran;

    public DataReqIzin(String sNama, String sStatus, boolean sKonfirmAdmin, boolean sKehadiran) {
        this.sNama = sNama;
        this.sStatus = sStatus;
        this.sKonfirmAdmin = sKonfirmAdmin;
        this.sKehadiran = sKehadiran;
    }

    public DataReqIzin() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getsNama() {
        return sNama;
    }

    public void setsNama(String sNama) {
        this.sNama = sNama;
    }

    public String getsStatus() {
        return sStatus;
    }

    public void setsStatus(String sStatus) {
        this.sStatus = sStatus;
    }

    public boolean issKonfirmAdmin() {
        return sKonfirmAdmin;
    }

    public void setsKonfirmAdmin(boolean sKonfirmAdmin) {
        this.sKonfirmAdmin = sKonfirmAdmin;
    }

    public boolean issKehadiran() {
        return sKehadiran;
    }

    public void setsKehadiran(boolean sKehadiran) {
        this.sKehadiran = sKehadiran;
    }
}
