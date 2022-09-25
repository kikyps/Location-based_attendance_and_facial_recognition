package com.absensi.inuraini.admin.datapegawai;

import com.absensi.inuraini.admin.jabatan.StoreJabatan;

import java.util.Comparator;

public class StoreDataPegawai {
    String key;
    String sNama;
    String sStatus;
    String sJabatan;

    public StoreDataPegawai(String key, String sNama, String sStatus, String sJabatan) {
        this.key = key;
        this.sNama = sNama;
        this.sStatus = sStatus;
        this.sJabatan = sJabatan;
    }

    public StoreDataPegawai() {
    }

    public static Comparator<StoreDataPegawai> storePegawaiComparator = new Comparator<StoreDataPegawai>() {
        @Override
        public int compare(StoreDataPegawai storePegawai, StoreDataPegawai t1) {
            if (storePegawai.getsNama() != null) {
                return storePegawai.getsNama().compareTo(t1.getsNama());
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

    public String getsJabatan() {
        return sJabatan;
    }

    public void setsJabatan(String sJabatan) {
        this.sJabatan = sJabatan;
    }
}
