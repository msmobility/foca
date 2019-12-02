package de.tum.bgu.msm.freight.io.output;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.CommodityGroup;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalMicroZone;
import de.tum.bgu.msm.freight.data.geo.MicroDepot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DistributionCenterCsvWriter {

    private int order = 0;

    public void writeToCsv(DataSet dataSet, String fileName) throws FileNotFoundException {

        PrintWriter pw = new PrintWriter(new File(fileName));

        pw.println("object,zone,dcId,dcName,dcX,dcY,commodityGroup,microDepotId,microDepotName,microDepotX,microDepotY,microZoneId,order");



        for (int zoneId : dataSet.getDistributionCenters().keySet()){
            Map<CommodityGroup, Map<Integer, DistributionCenter>> dcAtThisZone = dataSet.getDistributionCenters().get(zoneId);
            for (CommodityGroup commodityGroup : dcAtThisZone.keySet()){
                Map<Integer, DistributionCenter> dcAtThisZoneAndCommodity = dcAtThisZone.get(commodityGroup);
                for (int dcId : dcAtThisZoneAndCommodity.keySet()){
                    String object = "distributionCenter";
                    DistributionCenter dc = dcAtThisZoneAndCommodity.get(dcId);
                    String dcName = dc.getName();
                    double dcX = dc.getCoordinates().x;
                    double dcY = dc.getCoordinates().y;

                    int idMicroDepot = -1;
                    String nameMicroDepot = "-1";
                    double xMicroDepot = -1;
                    double yMicroDepot = -1;
                    int idMicroZone = -1;

                    pw.println(buildCsvLine(zoneId, commodityGroup, dcId, object, dcName, dcX, dcY,idMicroDepot, nameMicroDepot, xMicroDepot, yMicroDepot, idMicroZone));

                    Set<Integer> controlList = new HashSet<>();
                    for (InternalMicroZone internalMicroZOne : dc.getZonesServedByThis()){
                        dcX = -1;
                        dcY = -1;
                        dcName = "-1";
                        object = "catchmentArea";
                        idMicroZone =  internalMicroZOne.getId();
                        if (!controlList.contains(idMicroZone)){
                            pw.println(buildCsvLine(zoneId, commodityGroup, dcId, object, dcName, dcX, dcY,idMicroDepot, nameMicroDepot, xMicroDepot, yMicroDepot, idMicroZone));
                            controlList.add(idMicroZone);
                        }

                    }

                    for (MicroDepot microDepot : dc.getMicroDeportsServedByThis()) {
                        object = "microDepot";
                        idMicroDepot = microDepot.getId();
                        nameMicroDepot = microDepot.getName();
                        xMicroDepot = microDepot.getCoord_gk4().x;
                        yMicroDepot = microDepot.getCoord_gk4().y;
                        idMicroZone = microDepot.getMicroZoneId();
                        pw.println(buildCsvLine(zoneId, commodityGroup, dcId, object, dcName, dcX, dcY,idMicroDepot, nameMicroDepot, xMicroDepot, yMicroDepot, idMicroZone));
                       controlList = new HashSet<>();
                        for (InternalMicroZone internalMicroZOne : microDepot.getZonesServedByThis()){
                            object = "microDepotCatchmentArea";
                            nameMicroDepot = "-1";
                            xMicroDepot = -1;
                            yMicroDepot =  -1;
                            idMicroZone =  internalMicroZOne.getId();
                            if (!controlList.contains(idMicroZone)) {
                                pw.println(buildCsvLine(zoneId, commodityGroup, dcId, object, dcName, dcX, dcY, idMicroDepot, nameMicroDepot, xMicroDepot, yMicroDepot, idMicroZone));
                                controlList.add(idMicroZone);
                            }
                        }
                    }
                }
            }
        }

        pw.close();
    }

    private StringBuilder buildCsvLine(int zoneId, CommodityGroup commodityGroup, int dcId, String object, String dcName, double dcX, double dcY,int idMicroDepot, String nameMicroDepot, double xMicroDepot, double yMicroDepot, int idMicroZone) {
        StringBuilder sb = new StringBuilder();
        return sb.append(object).append(","). //obj
                append(zoneId).append(","). //zone
                append(dcId).append(","). //dcId
                append(dcName).append(","). //dcName
                append(dcX).append(","). //x
                append(dcY).append(","). //y
                append(commodityGroup).append(","). //commo
                append(idMicroDepot).append(","). //mdId
                append(nameMicroDepot).append(","). //mdName
                append(xMicroDepot).append(","). //mdx
                append(yMicroDepot).append(","). //mdY
                append(idMicroZone).append(","). //microZone
                append(order++);
    }
}
