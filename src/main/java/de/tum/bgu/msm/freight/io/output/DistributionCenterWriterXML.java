package de.tum.bgu.msm.freight.io.output;

import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.io.MatsimXmlWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DistributionCenterWriterXML extends MatsimXmlWriter {


    public static void main(String[] args) {

        new DistributionCenterWriterXML().write("c:/models/freightFlows/example.xml");
    }


    void write(String file) {


        List<Tuple<String, String>> dcAttributes = new ArrayList<>();
        dcAttributes.add(new Tuple<>("name", "dc1"));
        dcAttributes.add(new Tuple<>("size", "1000"));

        openFile(file);
        writeXmlHead();

        writeStartTag("distrubutionCenter", dcAttributes, false);
        writeStartTag("catchmentArea", new ArrayList<>(), false);

        for (int i=0; i< 10; i++){
            List<Tuple<String, String>> microZoneList = new ArrayList<>();
            microZoneList.add(new Tuple<>("id",String.valueOf(i)));
            writeStartTag("microZone", microZoneList, true);
        }

        writeEndTag("catchmentArea");
        writeEndTag("distributionCenter");


        close();
    }





    private void startDistributionCenter() {

    }

    private void endDistributionCenter() {

    }

    private void startMicroDepot() {

    }

    private void endMicroDepot() {

    }

    private void startCatchmentArea() {


    }

    private void endCatchmentArea() {

    }

    private void startMicroDepotCatchmentArea() {

    }

    private void endMicroDeportCatchmentArea() {

    }


}
