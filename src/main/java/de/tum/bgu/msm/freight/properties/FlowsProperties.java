package de.tum.bgu.msm.freight.properties;

import de.tum.bgu.msm.properties.PropertiesUtil;

import java.util.ResourceBundle;

public class FlowsProperties extends PropertiesGroup {

    private int[] commodityFlowYears = new int[]{2010,2030,2050};
    private String matrixFolder = "./input/matrices/";


    public FlowsProperties(ResourceBundle bundle) {
        super(bundle);

        String matrixFolderFromFile = PropertiesUtil.getStringProperty(bundle, "matrixFolder");
        if (!matrixFolderFromFile.isEmpty()) {
            this.matrixFolder = PropertiesUtil.getStringProperty(bundle, "matrixFolder");
        }

        String commodityFlowYearsFromFile = PropertiesUtil.getStringProperty(bundle, "commodityFlowYears");
        if (!commodityFlowYearsFromFile.isEmpty()) {
            this.commodityFlowYears = PropertiesUtil.getIntPropertyArray(bundle, "commodityFlowYears");
        }
    }

    public int[] getCommodityFlowsYears() {
        return commodityFlowYears;

    }

    public String getMatrixFolder() {
        return matrixFolder;
    }

    public void setMatrixFolder(String matrixFolder, Properties properties) {
        this.matrixFolder = matrixFolder;
    }
}
