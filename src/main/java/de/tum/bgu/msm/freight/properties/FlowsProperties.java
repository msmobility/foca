package de.tum.bgu.msm.freight.properties;

import de.tum.bgu.msm.properties.PropertiesUtil;

import java.util.ResourceBundle;

public class FlowsProperties extends PropertiesGroup {

    private String matrixFileNamePrefix = "ketten-uniform-";
    private String matrixFileNameSuffix = ".csv";
    private String commodityAttributeFile = "input/commodities/commodity_groups_kba_ipf.csv";
    private String terminalsFileName = "input/distributionCenters/intermodal_terminals_31468.csv";
    private double flowsScaleFactor = 1.;
    private int[] commodityFlowYears = new int[]{2010, 2030, 2050};
    private String matrixFolder = "./input/matrices/";



    public FlowsProperties(ResourceBundle bundle) {
        super(bundle);
        matrixFileNamePrefix = PropertiesUtil.getStringProperty(bundle, "matrixFileNamePrefix", matrixFileNamePrefix);
        matrixFileNameSuffix = PropertiesUtil.getStringProperty(bundle, "matrixFileNameSuffix", matrixFileNameSuffix);
        commodityAttributeFile = PropertiesUtil.getStringProperty(bundle, "commodityAttributeFile", commodityAttributeFile);
        terminalsFileName = PropertiesUtil.getStringProperty(bundle, "terminalsFileName", terminalsFileName);
        flowsScaleFactor = PropertiesUtil.getDoubleProperty(bundle, "flowsScaleFactor", flowsScaleFactor);

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

    public String getCommodityAttributeFile() {
        return commodityAttributeFile;
    }

    public void setCommodityAttributeFile(String commodityAttributeFile, Properties properties) {
        this.commodityAttributeFile = commodityAttributeFile;
    }

    public String getTerminalsFile() {
        return terminalsFileName;
    }

    public double getFlowsScaleFactor() {
        return flowsScaleFactor;
    }

    public void setFlowsScaleFactor(double flowsScaleFactor) {
        this.flowsScaleFactor = flowsScaleFactor;
    }

    public String getMatrixFileNamePrefix() {
        return matrixFileNamePrefix;
    }

    public String getMatrixFileNameSuffix() {
        return matrixFileNameSuffix;
    }
}
