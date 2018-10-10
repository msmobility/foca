package de.tum.bgu.msm.freight.properties;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Properties {

    public static final String zoneInputFile = "./input/zones_edit.csv";
    public static final String zoneShapeFile = "./input/shp/de_lkr_4326.shp";
    public static final String munichMicroZonesShapeFile = "input/shp/zones_4326.shp";
    public static final String regensburgMicroZonesShapeFile = "input/shp/zones_regensburg_4326.shp";
    public static final String idFieldInZonesShp = "RS";
    public static final String idFieldInMicroZonesShp = "id";

    public static final String matrixFileName = "./input/matrices/ketten-2010.csv";

    public static final double tons_by_truck = 10.;

    public static final String networkFile = "./networks/matsim/final.xml.gz";

    public static final int iterations = 10;

    public static final double scaleFactor = 1;
    public static final String runId = "test";

    public static Random rand = new Random(1);

    public static final int[] selectedDestinations = new int[]{9162,9362};



}
