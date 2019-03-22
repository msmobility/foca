package de.tum.bgu.msm.freight.data.freight;

public enum Transaction {

    /**
     * a private company that sends/receives an individual parcel or a small group of them
     */
    BUSINESS_CUSTOMER(0.25,0.25),
    /**
     * a person that sends/receives an individual parcel or a small group of them
     */
    PRIVATE_CUSTOMER(0.25, 0.00),
    /**
     * a parcel or a small group of parcels send or received through a parcel shop
     */
    PARCEL_SHOP(0.05,0.30),
    /**
     * a warehouse that sends or receives large groups of parcels as one of the ends
     *  of the trip - not a distribution center but a trip end!
     */
    WAREHOUSE(0.45,0.45);

    private double shareDeliveriesAtCustomer;
    private double sharePickupsAtCustomer;

    Transaction(double shareDeliveriesAtCustomer, double sharePickupsAtCustomer) {
        this.shareDeliveriesAtCustomer = shareDeliveriesAtCustomer;
        this.sharePickupsAtCustomer = sharePickupsAtCustomer;
    }

    public double getShareDeliveriesAtCustomer() {
        return shareDeliveriesAtCustomer;
    }

    public double getSharePickupsAtCustomer() {
        return sharePickupsAtCustomer;
    }
}
