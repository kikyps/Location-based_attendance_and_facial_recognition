package com.absensi.inuraini.admin.location;

import java.util.HashMap;
import java.util.Map;

public class DataKordinat {
    String sLatitude;
    String sLongitude;
    String sAddress;

    public DataKordinat(String sLatitude, String sLongitude, String sAddress) {
        this.sLatitude = sLatitude;
        this.sLongitude = sLongitude;
        this.sAddress = sAddress;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("sLatitude", sLatitude);
        result.put("sLongitude", sLongitude);
        result.put("sAddress", sAddress);
        return result;
    }

    public String getsAddress() {
        return sAddress;
    }

    public void setsAddress(String sAddress) {
        this.sAddress = sAddress;
    }

    public String getsLatitude() {
        return sLatitude;
    }

    public void setsLatitude(String sLatitude) {
        this.sLatitude = sLatitude;
    }

    public String getsLongitude() {
        return sLongitude;
    }

    public void setsLongitude(String sLongitude) {
        this.sLongitude = sLongitude;
    }
}
