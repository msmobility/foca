package de.tum.bgu.msm.freight.data;

public enum DistanceBin {


    D0_50,D50_150,D150_PLUS;

    public static DistanceBin getDistanceBin(double distance){
        if (distance < 50){
            return D0_50;
        } else if (distance < 150){
            return D50_150;
        }else {
            return D150_PLUS;
        }
    }
}
