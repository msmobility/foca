package de.tum.bgu.msm.freight.io.output;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.resources.Properties;
import de.tum.bgu.msm.resources.Resources;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class OutputWriter {

    private static final Logger logger = Logger.getLogger(OutputWriter.class);

    public static void printOutObjects(List<?> objects, String header, String path) {

        try {
            PrintWriter pw = new PrintWriter(new File (path));
            pw.println(header);
            for (Object object : objects){
                pw.println(object.toString());
            }

            pw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }



}


