package de.tum.bgu.msm.freight.properties;

import java.util.Random;
import java.util.ResourceBundle;

public class Properties {

    public static String zoneInputFile = "./input/zones_edit.csv";
    public static String zoneShapeFile = "./input/shp/de_lkr_4326.shp";
    public static String munichMicroZonesShapeFile = "input/shp/zones_4326.shp";
    public static String regensburgMicroZonesShapeFile = "input/shp/zones_regensburg_4326.shp";
    public static String idFieldInZonesShp = "RS";
    public static String idFieldInMicroZonesShp = "id";

    public static String matrixFileName = "./input/matrices/ketten-2010.csv";

    public static double tons_by_truck = 10.;

    public static String networkFile = "./networks/matsim/final_v2.xml.gz";
    public static String simpleNetworkFile = "./networks/matsim/europe.xml.gz";

    public static int iterations = 1;

    public static double scaleFactor = 1;
    public static String runId = "test";

    public static Random rand = new Random(1);

    public static final int[] selectedDestinations = new int[]{9162,9362};
//    public static int[] selectedDestinations = new int[]{-1};

    public static final boolean storeExpectedTimes = false;

}
