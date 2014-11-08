/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora.api.command;

/**
 * Set an individual Dimmer's light level.
 *
 * @author Dan Noguerol
 */
public class SetDimmerLevel extends RadioRaCommand {
    public static final String TYPE = "SDL";

    private int zoneNumber;
    private int dimmerLevel;
    private Integer fadeTime;
    private String system;

    public SetDimmerLevel(int zoneNumber, int dimmerLevel) {
        super(TYPE);
        this.zoneNumber = zoneNumber;
        this.dimmerLevel = dimmerLevel;
    }

    public SetDimmerLevel(int zoneNumber, int dimmerLevel, Integer fadeTime) {
        this(zoneNumber, dimmerLevel);
        this.fadeTime = fadeTime;
    }

    public int getZoneNumber() {
        return zoneNumber;
    }

    public int getDimmerLevel() {
        return dimmerLevel;
    }

    public boolean hasFadeTime() {
        return (fadeTime != null);
    }

    public Integer getFadeTime() {
        return fadeTime;
    }

    public String getSystem() {
        return system;
    }
}
