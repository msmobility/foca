package de.tum.bgu.msm.freight.properties;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;

public abstract class PropertiesGroup {

    public final Logger LOGGER = Logger.getLogger(PropertiesGroup.class);

    protected void logUsedProperties(){
        for (Field x : this.getClass().getDeclaredFields()) {
            x.setAccessible(true);
            try {
                LOGGER.info(x.getName() + ": " + x.get(this).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                //do nothing
            }
        }
    }

}
