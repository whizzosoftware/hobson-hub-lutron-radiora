/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora.api.command;

/**
 * Turn an individual Switch ON or OFF.
 *
 * @author Dan Noguerol
 */
public class SetSwitchLevel extends RadioRaCommand {
    public static final String TYPE = "SSL";

    private int zoneNumber;
    private boolean on;
    private Integer delayTime;

    public SetSwitchLevel(int zoneNumber, boolean on) {
        super(TYPE);
        this.zoneNumber = zoneNumber;
        this.on = on;
    }

    public SetSwitchLevel(int zoneNumber, boolean on, Integer delayTime) {
        this(zoneNumber, on);
        this.delayTime = delayTime;
    }

    public int getZoneNumber() {
        return zoneNumber;
    }

    public boolean isOn() {
        return on;
    }

    public Integer getDelayTime() {
        return delayTime;
    }

    public boolean hasDelayTime() {
        return (delayTime != null);
    }
}
