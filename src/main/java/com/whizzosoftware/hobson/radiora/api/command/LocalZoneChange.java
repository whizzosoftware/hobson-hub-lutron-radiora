/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora.api.command;

public class LocalZoneChange extends RadioRaCommand {
    public static final String TYPE = "LZC";

    private int zoneNumber;
    private State state;

    public LocalZoneChange(int zoneNumber, State state) {
        super(TYPE);
        this.zoneNumber = zoneNumber;
        this.state = state;
    }

    public int getZoneNumber() {
        return zoneNumber;
    }

    public State getState() {
        return state;
    }

    public enum State {
        /** Level went from OFF to ON */
        ON,
        /** Level went from ON to OFF */
        OFF,
        /** Level went from ON level to another ON level */
        CHG
    }
}
