/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora.api;

import com.whizzosoftware.hobson.radiora.api.command.LEDMap;
import com.whizzosoftware.hobson.radiora.api.command.LocalZoneChange;
import com.whizzosoftware.hobson.radiora.api.command.ZoneMap;

/**
 * A delegate interface for receiving RadioRa data.
 *
 * @author Dan Noguerol
 */
public interface RadioRaChannelDelegate {
    public void onLocalZoneChange(LocalZoneChange o);
    public void onZoneMap(ZoneMap o);
    public void onLEDMap(LEDMap o);
}
