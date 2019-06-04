package de.tum.bgu.msm.freight.modules.common;

import com.google.common.collect.HashBasedTable;
import de.tum.bgu.msm.freight.FreightFlowUtils;
import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.data.geo.InternalMicroZone;
import de.tum.bgu.msm.freight.data.geo.InternalZone;

import java.util.*;

public class SpatialDisaggregator {


    /**
     *
     * @param commodity
     * @param internalMicroZones
     * @param makeOrUseTable
     * @return
     */
    public static int disaggregateToMicroZoneBusiness(Commodity commodity, Collection<InternalMicroZone> internalMicroZones, HashBasedTable<String, Commodity, Double> makeOrUseTable) {

        Map<Integer, Double> microZonesProbabilities = new HashMap<>();

        Map<String, Double> coefficientsForThisCommodity = makeOrUseTable.column(commodity);
        Set<String> jobTypes = makeOrUseTable.rowKeySet();

        for (InternalMicroZone microZone : internalMicroZones) {
            double utility = 0;
            for (String jobType : jobTypes){
                utility += microZone.getAttribute(jobType) * coefficientsForThisCommodity.get(jobType);
            }
            microZonesProbabilities.put(microZone.getId(), utility);
        }
        return FreightFlowUtils.select(microZonesProbabilities,
                FreightFlowUtils.getSum(microZonesProbabilities.values()));

    }

    public static int disaggregateToMicroZonePrivate(Collection<InternalMicroZone> internalMicroZones) {

        Map<Integer, Double> microZonesProbabilities = new HashMap<>();

       for (InternalMicroZone microZone : internalMicroZones) {
            microZonesProbabilities.put(microZone.getId(), microZone.getAttribute("population"));
        }
        return FreightFlowUtils.select(microZonesProbabilities,
                FreightFlowUtils.getSum(microZonesProbabilities.values()));

    }
}
