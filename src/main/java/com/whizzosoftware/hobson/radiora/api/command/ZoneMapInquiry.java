/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora.api.command;

/**
 * Request an updated Zone Map.
 *
 * @author Dan Noguerol
 */
public class ZoneMapInquiry extends RadioRaCommand {
    public static final String TYPE = "ZMPI";

    public ZoneMapInquiry() {
        super(TYPE);
    }
}
