package de.tum.bgu.msm.freight.data;

public enum DistanceBin {


    D0_50,D50_100,D100_200, D200_500, D500_PLUS;

    public static DistanceBin getDistanceBin(double distance){
        if (distance < 50){
            return D0_50;
        } else if (distance < 100){
            return D50_100;
        }else if (distance < 200){
            return D100_200;
        } else if (distance < 500){
            return D200_500;
        } else {
            return D500_PLUS;
        }
    }
}
