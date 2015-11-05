package com.canonical.democlient; /**
 * Created by gerald on 2015/11/4.
 */

import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class DemoResource {
    public static final String SENSOR_TEMPERATURE_KEY = "temperature";
    public static final String SENSOR_LIGHT_KEY = "light";
    public static final String SENSOR_SOUND_KEY = "sound";

    private double mTemp;
    private int mLight;
    private int mSound;


    private int mTemp_index;
    private int mLight_index;
    private int mSound_index;

    public DemoResource() {
        mTemp = 0.0;
        mLight = 0;
        mLight = 0;

        mTemp_index = 0;
        mLight_index = 0;
        mSound_index = 0;
    }

    public void sensorSetOcRepresentation(OcRepresentation rep) throws OcException {
        mTemp = rep.getValue(DemoResource.SENSOR_TEMPERATURE_KEY);
        mLight = rep.getValue(DemoResource.SENSOR_LIGHT_KEY);
        mSound = rep.getValue(DemoResource.SENSOR_SOUND_KEY);
    }

    public OcRepresentation getOcRepresentation() throws OcException {
        OcRepresentation rep = new OcRepresentation();
        rep.setValue(SENSOR_TEMPERATURE_KEY, mTemp);
        rep.setValue(SENSOR_LIGHT_KEY, mLight);
        rep.setValue(SENSOR_SOUND_KEY, mSound);
        return rep;
    }

    public double getTemp() {
        return mTemp;
    }

    public int getLight() {
        return mLight;
    }

    public int getSound() {
        return mSound;
    }

    public void setTempIndex(int index) {
        mTemp_index = index;
    }

    public void setLightIndex(int index) {
        mLight_index = index;
    }

    public void setSoundIndex(int index) {
        mSound_index = index;
    }

    public int getTempIndex() {
        return mTemp_index;
    }

    public int getLightIndex() {
        return mLight_index;
    }

    public int getSoundIndex() {
        return mSound_index;
    }

}
