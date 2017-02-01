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

import com.whizzosoftware.hobson.api.device.HobsonDeviceDescriptor;
import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.event.MockEventManager;
import com.whizzosoftware.hobson.api.event.device.DeviceVariablesUpdateEvent;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.variable.DeviceVariableUpdate;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.radiora.api.command.LocalZoneChange;
import com.whizzosoftware.hobson.radiora.api.command.ZoneMap;
import org.junit.Test;
import static org.junit.Assert.*;

public class RadioRaPluginTest {
    @Test
    public void testOnZoneMap() throws Exception {
        final RadioRaPlugin plugin = new RadioRaPlugin("id", "1.0", null);
        MockDeviceManager deviceManager = new MockDeviceManager();
        MockEventManager eventManager = new MockEventManager();
        plugin.setDeviceManager(deviceManager);
        plugin.setEventManager(eventManager);

        // send valid zone map with two devices
        assertEquals(0, deviceManager.getPublishedDeviceCount(plugin.getContext()));
        assertEquals(0, eventManager.getEventCount());
        plugin.onZoneMap(new ZoneMap("01XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"), true);
        assertEquals(2, deviceManager.getPublishedDeviceCount(plugin.getContext()));
        assertEquals(4, eventManager.getEventCount());
        eventManager.clearEvents();
        assertEquals(0, eventManager.getEventCount());

        // send invalid zone map
        eventManager.clearEvents();
        plugin.onZoneMap(new ZoneMap("01XXXXX"));
        assertEquals(0, eventManager.getEventCount());

        // send valid zone map with no devices
        eventManager.clearEvents();
        plugin.onZoneMap(new ZoneMap("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"), true);
        assertEquals(0, eventManager.getEventCount());

        // verify that the devices were removed
        assertEquals(0, deviceManager.getPublishedDeviceCount(plugin.getContext()));
    }

    @Test
    public void testOnLocalZoneChange() {
        MockDeviceManager deviceManager = new MockDeviceManager();
        MockEventManager eventManager = new MockEventManager();
        RadioRaPlugin plugin = new RadioRaPlugin("id", "1.0", null);
        plugin.setDeviceManager(deviceManager);
        plugin.setEventManager(eventManager);

        // force creation of zone 1 device
        plugin.onZoneMap(new ZoneMap("1XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"), true);
        assertEquals(2, eventManager.getEventCount());
        eventManager.clearEvents();
        assertEquals(0, eventManager.getEventCount());

        // send local zone change
        plugin.onLocalZoneChange(new LocalZoneChange(1, LocalZoneChange.State.ON));
        assertEquals(1, eventManager.getEventCount());
        DeviceVariablesUpdateEvent e = (DeviceVariablesUpdateEvent)eventManager.getEvent(0);
        assertEquals(1, e.getUpdates().size());
        DeviceVariableUpdate vu = e.getUpdates().iterator().next();
        assertEquals(VariableConstants.ON, vu.getName());
        assertTrue((Boolean)vu.getNewValue());

        eventManager.clearEvents();
        assertEquals(0, eventManager.getEventCount());
        plugin.onLocalZoneChange(new LocalZoneChange(1, LocalZoneChange.State.OFF));
        assertEquals(1, eventManager.getEventCount());
        e = (DeviceVariablesUpdateEvent)eventManager.getEvent(0);
        vu = e.getUpdates().iterator().next();
        assertEquals(VariableConstants.ON, vu.getName());
        assertFalse((Boolean)vu.getNewValue());

        eventManager.clearEvents();
        assertEquals(0, eventManager.getEventCount());
        plugin.onLocalZoneChange(new LocalZoneChange(1, LocalZoneChange.State.CHG));
        assertEquals(1, eventManager.getEventCount());
        e = (DeviceVariablesUpdateEvent)eventManager.getEvent(0);
        assertEquals(1, e.getUpdates().size());
        vu = e.getUpdates().iterator().next();
        assertEquals(VariableConstants.ON, vu.getName());
        assertTrue((Boolean)vu.getNewValue());
    }

    @Test
    public void testDisconnectDeviceAvailability() {
        MockDeviceManager deviceManager = new MockDeviceManager();
        MockEventManager eventManager = new MockEventManager();
        RadioRaPlugin plugin = new RadioRaPlugin("id", "1.0", null);
        plugin.setDeviceManager(deviceManager);
        plugin.setEventManager(eventManager);

        // send valid zone map with two devices
        assertEquals(0, plugin.getDeviceCount());

        plugin.onZoneMap(new ZoneMap("0XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"), true);

        assertEquals(1, plugin.getDeviceCount());

        for (HobsonDeviceDescriptor d : deviceManager.getDevices(HubContext.createLocal())) {
            assertTrue(deviceManager.isDeviceAvailable(d.getContext()));
        }
        plugin.onChannelDisconnected();
        for (HobsonDeviceDescriptor d : deviceManager.getDevices(HubContext.createLocal())) {
            assertFalse(deviceManager.isDeviceAvailable(d.getContext()));
        }

        plugin.onChannelConnected();
        plugin.onZoneMap(new ZoneMap("1XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"), true);
        for (HobsonDeviceDescriptor d : deviceManager.getDevices(HubContext.createLocal())) {
            assertTrue(deviceManager.isDeviceAvailable(d.getContext()));
        }
    }
}