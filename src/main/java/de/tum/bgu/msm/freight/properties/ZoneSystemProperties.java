package de.tum.bgu.msm.freight.properties;

import de.tum.bgu.msm.properties.PropertiesUtil;

import java.util.ResourceBundle;

public class ZoneSystemProperties extends PropertiesGroup {

    private String zoneInputFile = "./input/zones_edit_31468.csv";
    private String zoneShapeFile = "./input/shp/de_lkr_31468.shp";
    private String munichMicroZonesShapeFile = "input/shp/zones_31468_jobs.shp";
    private String regensburgMicroZonesShapeFile = "input/shp/zones_regensburg_31468_jobs.shp";
    private String idFieldInZonesShp = "RS";
    private String idFieldInMicroZonesShp = "id";

    public ZoneSystemProperties(ResourceBundle bundle) {
        super(bundle);
        PropertiesUtil.newPropertySubmodule("Zone system");
        zoneInputFile = PropertiesUtil.getStringProperty(bundle, "zoneInputFile", zoneInputFile);
        zoneShapeFile = PropertiesUtil.getStringProperty(bundle, "zoneShapeFile", zoneShapeFile);
        munichMicroZonesShapeFile = PropertiesUtil.getStringProperty(bundle, "munichMicroZonesShapeFile", munichMicroZonesShapeFile);
        regensburgMicroZonesShapeFile = PropertiesUtil.getStringProperty(bundle, "regensburgMicroZonesShapeFile", regensburgMicroZonesShapeFile);
        idFieldInZonesShp = PropertiesUtil.getStringProperty(bundle, "idFieldInZonesShp", idFieldInZonesShp);
        idFieldInMicroZonesShp = PropertiesUtil.getStringProperty(bundle, "idFieldInMicroZonesShp", idFieldInMicroZonesShp);
    }

    public String getZoneInputFile() {
        return zoneInputFile;
    }
    public String getZoneShapeFile() {
        return zoneShapeFile;
    }

    public String getIdFieldInZonesShp() {
        return idFieldInZonesShp;
    }

    public void setZoneInputFile(String zoneInputFile) {
        this.zoneInputFile = zoneInputFile;
    }

    public void setZoneShapeFile(String zoneShapeFile) {
        this.zoneShapeFile = zoneShapeFile;
    }

    public void setMunichMicroZonesShapeFile(String munichMicroZonesShapeFile) {
        this.munichMicroZonesShapeFile = munichMicroZonesShapeFile;
    }

    public void setRegensburgMicroZonesShapeFile(String regensburgMicroZonesShapeFile) {
        this.regensburgMicroZonesShapeFile = regensburgMicroZonesShapeFile;
    }

    public void setIdFieldInZonesShp(String idFieldInZonesShp) {
        this.idFieldInZonesShp = idFieldInZonesShp;
    }

    public String getIdFieldInMicroZonesShp() {
        return idFieldInMicroZonesShp;
    }

    public void setIdFieldInMicroZonesShp(String idFieldInMicroZonesShp) {
        this.idFieldInMicroZonesShp = idFieldInMicroZonesShp;
    }


    public String getMicroZonesShapeFile(int zoneId) {
        if (zoneId == 9162){
            return munichMicroZonesShapeFile;
        } else if (zoneId == 9362){
            return regensburgMicroZonesShapeFile;
        } else {
            throw new RuntimeException("This zone does not have micro zones: " + zoneId);
        }

    }
}
