/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora.api.command;

/**
 * Gives the state of all the zones.
 *
 * @author Dan Noguerol
 */
public class ZoneMap extends RadioRaCommand {
    public static final String TYPE = "ZMP";

    private String zoneMap;
    private String system;

    public ZoneMap(String zoneMap) {
        this(zoneMap, null);
    }

    public ZoneMap(String zoneMap, String system) {
        super(TYPE);
        this.zoneMap = zoneMap;
        this.system = system;
    }

    public String getZoneMap() {
        return zoneMap;
    }

    public String getSystem() {
        return system;
    }

    public String toString() {
        return zoneMap;
    }
}
