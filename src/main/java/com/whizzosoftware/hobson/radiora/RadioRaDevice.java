/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora;

import com.whizzosoftware.hobson.api.device.AbstractHobsonDevice;
import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Hobson device representing a RadioRa dimmer or switch.
 *
 * @author Dan Noguerol
 */
public class RadioRaDevice extends AbstractHobsonDevice {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean startupValue;

    public RadioRaDevice(RadioRaPlugin plugin, int zone, boolean startupValue) {
        super(plugin, Integer.toString(zone));
        setDefaultName("RadioRa Zone " + zone);
        this.startupValue = startupValue;
    }

    public void setStartupValue(boolean startupValue) {
        this.startupValue = startupValue;
    }

    @Override
    public void onStartup(PropertyContainer config) {
        super.onStartup(config);

        logger.debug("Device {} is starting", getContext());

        // publish the appropriate variable
        long now = System.currentTimeMillis();
        publishVariable(VariableConstants.ON, startupValue, HobsonVariable.Mask.READ_WRITE, now);
        publishVariable(VariableConstants.LEVEL, null, HobsonVariable.Mask.WRITE_ONLY, now);
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public DeviceType getType() {
        return DeviceType.LIGHTBULB;
    }

    @Override
    public String getManufacturerName() {
        return "Lutron";
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
    protected TypedProperty[] createSupportedProperties() {
        return null;
    }

    @Override
    public void onSetVariable(String name, Object value) {
        int zoneId = Integer.parseInt(getContext().getDeviceId());

        if (VariableConstants.ON.equals(name)) {
            Boolean val = getAsBoolean(value);
            if (val != null) {
                ((RadioRaPlugin)getPlugin()).setSwitchLevel(zoneId, val);
            } else {
                logger.error("Invalid \"on\" value specified: {}", value);
            }
        } else if (VariableConstants.LEVEL.equals(name)) {
            Integer val = getAsInteger(value);
            if (val != null) {
                ((RadioRaPlugin)getPlugin()).setDimmerLevel(zoneId, val);
            } else {
                logger.error("Invalid \"level\" value specified: {}", value);
            }
        }
    }

    protected Boolean getAsBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean)value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String)value);
        }
        return null;
    }

    protected Integer getAsInteger(Object value) {
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
