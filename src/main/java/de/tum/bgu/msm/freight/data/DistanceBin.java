package de.tum.bgu.msm.freight.data;

public enum DistanceBin {


    D0_50,D50_100,D100_200, D200_500, D500_PLUS;

    public static DistanceBin getDistanceBin(double distance){
        if (distance < 50*1.6){
            return D0_50;
        } else if (distance < 100*1.6){
            return D50_100;
        }else if (distance < 200*1.6){
            return D100_200;
        } else if (distance < 500*1.6){
            return D200_500;
        } else {
            return D500_PLUS;
        }
    }
}
