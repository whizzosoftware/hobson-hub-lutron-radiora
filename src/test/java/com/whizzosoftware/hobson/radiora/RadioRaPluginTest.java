/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora;

import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.variable.MockVariableManager;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.radiora.api.command.LocalZoneChange;
import com.whizzosoftware.hobson.radiora.api.command.ZoneMap;
import org.junit.Test;
import static org.junit.Assert.*;

public class RadioRaPluginTest {
    @Test
    public void testOnZoneMap() {
        RadioRaPlugin plugin = new RadioRaPlugin("id");
        MockDeviceManager deviceManager = new MockDeviceManager();
        plugin.setDeviceManager(deviceManager);
        MockVariableManager manager = new MockVariableManager();
        plugin.setVariableManager(manager);

        // send valid zone map with two devices
        assertEquals(0, plugin.getDeviceCount());
        assertEquals(0, manager.getVariableUpdates().size());
        plugin.onZoneMap(new ZoneMap("01XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"));
        assertEquals(2, plugin.getDeviceCount());
        assertEquals(2, manager.getVariableUpdates().size());
        assertEquals("1", manager.getVariableUpdates().get(0).getDeviceId());
        assertEquals(VariableConstants.ON, manager.getVariableUpdates().get(0).getName());
        assertFalse((Boolean) manager.getVariableUpdates().get(0).getValue());
        assertEquals("2", manager.getVariableUpdates().get(1).getDeviceId());
        assertEquals(VariableConstants.ON, manager.getVariableUpdates().get(1).getName());
        assertTrue((Boolean) manager.getVariableUpdates().get(1).getValue());

        // send invalid zone map
        manager.clearVariableUpdates();
        plugin.onZoneMap(new ZoneMap("01XXXXX"));
        assertEquals(0, manager.getVariableUpdates().size());

        // send valid zone map with no devices
        manager.clearVariableUpdates();
        plugin.onZoneMap(new ZoneMap("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"));
        assertEquals(0, manager.getVariableUpdates().size());

        // verify that the devices were removed
        assertEquals(0, plugin.getDeviceCount());
    }

    @Test
    public void testOnLocalZoneChange() {
        RadioRaPlugin plugin = new RadioRaPlugin("id");
        MockVariableManager manager = new MockVariableManager();
        plugin.setVariableManager(manager);

        assertEquals(0, manager.getVariableUpdates().size());
        plugin.onLocalZoneChange(new LocalZoneChange(1, LocalZoneChange.State.ON));
        assertEquals(1, manager.getVariableUpdates().size());
        VariableUpdate vu = manager.getVariableUpdates().get(0);
        assertEquals(VariableConstants.ON, vu.getName());
        assertTrue((Boolean)vu.getValue());

        manager.clearVariableUpdates();
        assertEquals(0, manager.getVariableUpdates().size());
        plugin.onLocalZoneChange(new LocalZoneChange(1, LocalZoneChange.State.OFF));
        assertEquals(1, manager.getVariableUpdates().size());
        vu = manager.getVariableUpdates().get(0);
        assertEquals(VariableConstants.ON, vu.getName());
        assertFalse((Boolean) vu.getValue());

        manager.clearVariableUpdates();
        assertEquals(0, manager.getVariableUpdates().size());
        plugin.onLocalZoneChange(new LocalZoneChange(1, LocalZoneChange.State.CHG));
        assertEquals(1, manager.getVariableUpdates().size());
        vu = manager.getVariableUpdates().get(0);
        assertEquals(VariableConstants.ON, vu.getName());
        assertTrue((Boolean) vu.getValue());
    }

    @Test
    public void testDisconnectVariableInvalidation() {
        RadioRaPlugin plugin = new RadioRaPlugin("id");
        MockDeviceManager deviceManager = new MockDeviceManager();
        plugin.setDeviceManager(deviceManager);
        MockVariableManager variableManager = new MockVariableManager();
        plugin.setVariableManager(variableManager);

        // send valid zone map with two devices
        assertEquals(0, plugin.getDeviceCount());
        assertEquals(0, variableManager.getVariableUpdates().size());

        plugin.onZoneMap(new ZoneMap("0XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"));

        assertEquals(1, plugin.getDeviceCount());
        assertEquals(1, variableManager.getVariableUpdates().size());

        VariableUpdate update = variableManager.getVariableUpdates().get(0);
        assertEquals("1", update.getDeviceId());
        assertEquals(VariableConstants.ON, update.getName());
        assertFalse((boolean)update.getValue());

        variableManager.clearVariableUpdates();
        assertEquals(0, variableManager.getVariableUpdates().size());
        plugin.onChannelDisconnected();

        assertEquals(1, variableManager.getVariableUpdates().size());
        update = variableManager.getVariableUpdates().get(0);
        assertEquals("1", update.getDeviceId());
        assertEquals(VariableConstants.ON, update.getName());
        assertNull(update.getValue());

        variableManager.clearVariableUpdates();
        assertEquals(0, variableManager.getVariableUpdates().size());

        plugin.onZoneMap(new ZoneMap("1XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"));

        update = variableManager.getVariableUpdates().get(0);
        assertEquals("1", update.getDeviceId());
        assertEquals(VariableConstants.ON, update.getName());
        assertTrue((boolean)update.getValue());
    }
}