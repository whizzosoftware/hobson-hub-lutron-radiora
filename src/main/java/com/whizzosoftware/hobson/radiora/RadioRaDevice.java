/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.radiora;

import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.device.proxy.AbstractHobsonDeviceProxy;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableMask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * A Hobson device representing a RadioRa dimmer or switch.
 *
 * @author Dan Noguerol
 */
public class RadioRaDevice extends AbstractHobsonDeviceProxy {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean startupValue;

    RadioRaDevice(RadioRaPlugin plugin, int zone, boolean startupValue) {
        super(plugin, Integer.toString(zone), "RadioRA Zone " + zone, DeviceType.LIGHTBULB);
        this.startupValue = startupValue;
    }

    @Override
    public void onStartup(String name, Map<String,Object> config) {
        logger.debug("Device {} is starting", getContext());

        // publish the appropriate variable
        long now = System.currentTimeMillis();
        publishVariables(
            createDeviceVariable(VariableConstants.ON, VariableMask.READ_WRITE, startupValue, now),
            createDeviceVariable(VariableConstants.LEVEL, VariableMask.WRITE_ONLY, null, now)
        );
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public String getManufacturerName() {
        return "Lutron";
    }

    @Override
    public String getManufacturerVersion() {
        return null;
    }

    @Override
    public String getModelName() {
        return "RadioRA";
    }

    @Override
    public String getPreferredVariableName() {
        return VariableConstants.ON;
    }

    @Override
    public void onDeviceConfigurationUpdate(Map<String,Object> config) {

    }

    @Override
    protected TypedProperty[] getConfigurationPropertyTypes() {
        return null;
    }

    @Override
    public void onSetVariables(Map<String,Object> values) {
        int zoneId = Integer.parseInt(getContext().getDeviceId());

        for (String name : values.keySet()) {
            Object value = values.get(name);
            if (VariableConstants.ON.equals(name)) {
                Boolean val = getAsBoolean(value);
                if (val != null) {
                    ((RadioRaPlugin) getPlugin()).setSwitchLevel(zoneId, val);
                } else {
                    logger.error("Invalid \"on\" value specified: {}", value);
                }
            } else if (VariableConstants.LEVEL.equals(name)) {
                Integer val = getAsInteger(value);
                if (val != null) {
                    ((RadioRaPlugin) getPlugin()).setDimmerLevel(zoneId, val);
                } else {
                    logger.error("Invalid \"level\" value specified: {}", value);
                }
            }
        }
    }

    void onZoneUpdate(boolean on) {
        if (isStarted()) {
            setVariableValue(VariableConstants.ON, on, System.currentTimeMillis());
            setLastCheckin(System.currentTimeMillis());
        } else {
            startupValue = on;
        }
    }

    private Boolean getAsBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean)value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String)value);
        }
        return null;
    }

    private Integer getAsInteger(Object value) {
        try {
            if (value instanceof Integer) {
                return (Integer)value;
            } else if (value instanceof String) {
                return Integer.parseInt((String)value);
            }
        } catch (NumberFormatException e) {
            // NO-OP
        }
        return null;
    }
}
