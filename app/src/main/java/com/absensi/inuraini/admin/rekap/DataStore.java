package com.absensi.inuraini.admin.rekap;

import java.util.Comparator;

public class DataStore {

    String key;
    String sNama;
    String sStatus;

    public DataStore(String key, String sNama, String sStatus) {
        this.key = key;
        this.sNama = sNama;
        this.sStatus = sStatus;
    }

    public DataStore() {
    }

    public static Comparator<DataStore> dataStoreComparator = (dataStore, t1) -> dataStore.getsNama().compareTo(t1.sNama);

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getsNama() {
        return sNama;
    }

    public String getsStatus() {
        return sStatus;
    }

    public void setsStatus(String sStatus) {
        this.sStatus = sStatus;
    }
}
