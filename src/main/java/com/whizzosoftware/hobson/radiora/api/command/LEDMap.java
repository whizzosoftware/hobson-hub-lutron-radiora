/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora.api.command;

/**
 * Gives the state of all the Phantom LEDs.
 *
 * @author Dan Noguerol
 */
public class LEDMap extends RadioRaCommand {
    public static final String TYPE = "LMP";

    private String states;

    public LEDMap(String states) {
        super(TYPE);
        this.states = states;
    }

    public String getStates() {
        return states;
    }

    public String toString() {
        return states;
    }
}
