package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.CommodityGroup;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.Terminal;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TerminalReader extends CSVReader {

    private static Logger logger = Logger.getLogger(TerminalReader.class);


    private int posId;
    private int posName;
    private int posX;
    private int posY;
    //private int posCommodity;
    private int posZone;

    private int counter = 0;

    private Properties properties;

    protected TerminalReader(DataSet dataSet, Properties properties) {
        super(dataSet);
        this.properties = properties;
    }

    @Override
    protected void processHeader(String[] header) {
        posId = MitoUtil.findPositionInArray("id", header);
        posName = MitoUtil.findPositionInArray("name", header);
        posX = MitoUtil.findPositionInArray("xcoord", header);
        posY = MitoUtil.findPositionInArray("ycoord", header);
        //posCommodity = MitoUtil.findPositionInArray("commodityGroup", header);
        posZone = MitoUtil.findPositionInArray("zone", header);

    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[posId]);
        String name = record[posName];
        double x = Double.parseDouble(record[posX]);
        double y = Double.parseDouble(record[posY]);
        //CommodityGroup commodityGroup = CommodityGroup.valueOf(record[posCommodity].toUpperCase());
        int zoneId = Integer.parseInt(record[posZone]);
        Coordinate coord;
        if (x == -1 || y == -1) {
            coord = dataSet.getZones().get(zoneId).getCoordinates();
        } else {
            coord = new Coordinate(x, y);
        }

        dataSet.getTerminals().put(id, new Terminal(id, name, coord, false));

        counter++;
    }

    @Override
    public void read() {
        super.read(properties.getTerminalsFile(), ",");
        logger.info("Read " + counter + " terminals.");

    }
}
