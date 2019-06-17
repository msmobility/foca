package de.tum.bgu.msm.freight.data.freight.urban;

import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.MicroDepot;
import de.tum.bgu.msm.freight.data.geo.ParcelShop;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;

public class Parcel {

    private final int id;
    private ParcelTransaction parcelTransaction;
    private final boolean toDestination;
    private final double volume_m3;
    private final double weight_kg;
    private final Commodity commodity;
    private final DistributionCenter distributionCenter;

    private ParcelDistributionType parcelDistributionType;
    private MicroDepot microDepot = null;
    private ParcelShop parcelShop = null;
    private Coordinate originCoord;
    private Coordinate destCoord;
    private int origMicroZoneId;
    private int destMicroZoneId;

    private boolean assigned = false;

    public Parcel(int id, boolean toDestination, double volume_m3, double weight_kg,
                  DistributionCenter distributionCenter, Commodity commodity) {
        this.id = id;
        this.toDestination = toDestination;
        this.volume_m3 = volume_m3;
        this.weight_kg = weight_kg;
        this.distributionCenter = distributionCenter;
        this.commodity = commodity;
    }

    public static String getHeader() {

        StringBuilder builder = new StringBuilder();

        builder.append("id").append(",").
                append("toDestination").append(",").
                append("weight_kg").append(",").
                append("commodity").append(",").
                append("originX").append(",").
                append("originY").append(",").
                append("destX").append(",").
                append("destY").append(",").
                append("origMicroZone").append(",").
                append("destMicroZone").append(",").
                append("distributionCenter").append(",").
                append("transaction").append(",").
                append("microDepot").append(",").
                append("parcelShop").append(",").
                append("distributionType").append(",").
                append("assigned");


        return builder.toString();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(id).append(",").
                append(toDestination).append(",").
                append(weight_kg).append(",").
                append(commodity).append(",");

        if (this.getParcelTransaction().equals(ParcelTransaction.BUSINESS_CUSTOMER) || this.getParcelTransaction().equals(ParcelTransaction.PRIVATE_CUSTOMER)) {
            builder.append(originCoord.getX()).append(",").
                    append(originCoord.getY()).append(",").
                    append(destCoord.getX()).append(",").
                    append(destCoord.getY()).append(",").
                    append(origMicroZoneId).append(",").
                    append(destMicroZoneId).append(",");
        } else {
            builder.append("null").append(",").
                    append("null").append(",").
                    append("null").append(",").
                    append("null").append(",").
                    append("null").append(",").
                    append("null").append(",");
        }


        builder.append(distributionCenter.getId()).append(",").
                append(parcelTransaction.toString()).append(",");

        if (microDepot != null){
            builder.append(microDepot.getId()).append(",");
        } else {
            builder.append("null").append(",");
        }

                builder.append(parcelShop).append(",").
                append(parcelDistributionType).append(",").
                append(assigned);

        return builder.toString();

    }

    public int getId() {
        return id;
    }

    public ParcelTransaction getParcelTransaction() {
        return parcelTransaction;
    }

    public void setParcelTransaction(ParcelTransaction parcelTransaction) {
        this.parcelTransaction = parcelTransaction;
    }

    public boolean isToDestination() {
        return toDestination;
    }

    public double getVolume_m3() {
        return volume_m3;
    }

    public double getWeight_kg() {
        return weight_kg;
    }

    public DistributionCenter getDistributionCenter() {
        return distributionCenter;
    }

    public Coordinate getOriginCoord() {
        return originCoord;
    }

    public void setOriginCoord(Coordinate originCoord) {
        this.originCoord = originCoord;
    }

    public Coordinate getDestCoord() {
        return destCoord;
    }

    public void setDestCoord(Coordinate destCoord) {
        this.destCoord = destCoord;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public void setOrigMicroZone(int microZone) {
        this.origMicroZoneId = microZone;
    }

    public void setDestMicroZone(int microZone) {
        this.destMicroZoneId = microZone;
    }

    public int getDestMicroZoneId() {
        return destMicroZoneId;
    }

    public int getOrigMicroZoneId(){
        return origMicroZoneId;
    }

    public ParcelDistributionType getParcelDistributionType() {
        return parcelDistributionType;
    }

    public void setParcelDistributionType(ParcelDistributionType parcelDistributionType) {
        this.parcelDistributionType = parcelDistributionType;
    }

    public MicroDepot getMicroDepot() {
        return microDepot;
    }

    public void setMicroDepot(MicroDepot microDepot) {
        this.microDepot = microDepot;
    }

    public ParcelShop getParcelShop() {
        return parcelShop;
    }

    public void setParcelShop(ParcelShop parcelShop) {
        this.parcelShop = parcelShop;
    }

    public void setAssigned() {
    }
}
