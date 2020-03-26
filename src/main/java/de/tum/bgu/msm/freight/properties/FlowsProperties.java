package de.tum.bgu.msm.freight.properties;

import de.tum.bgu.msm.properties.PropertiesUtil;

import java.util.ResourceBundle;

public class FlowsProperties extends PropertiesGroup {

    private int[] commodityFlowYears = new int[]{2010,2030,2050};
    private String matrixFolder = "./input/matrices/";


    public FlowsProperties(ResourceBundle bundle) {
        super(bundle);
        matrixFolder = PropertiesUtil.getStringProperty(bundle, "matrixFolder", matrixFolder);
       commodityFlowYears = PropertiesUtil.getIntPropertyArray(bundle, "commodityFlowYears", commodityFlowYears);

    }

    public int[] getCommodityFlowsYears() {
        return commodityFlowYears;

    }

    public String getMatrixFolder() {
        return matrixFolder;
    }

    public void setMatrixFolder(String matrixFolder) {
        this.matrixFolder = matrixFolder;
    }
}
