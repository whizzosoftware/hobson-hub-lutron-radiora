/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.radiora;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.plugin.channel.AbstractChannelObjectPlugin;
import com.whizzosoftware.hobson.api.plugin.channel.ChannelIdleDetectionConfig;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.radiora.api.codec.RadioRaFrameDecoder;
import com.whizzosoftware.hobson.radiora.api.codec.RadioRaFrameEncoder;
import com.whizzosoftware.hobson.radiora.api.command.*;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.rxtx.RxtxChannelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A Hobson driver that controls a Lutron RadioRa system via a RA-RS232 interface.
 *
 * @author Dan Noguerol
 */
public class RadioRaPlugin extends AbstractChannelObjectPlugin {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static int IDLE_DETECTION_INTERVAL = 10;

    private final Map<Integer,RadioRaDevice> devices = new HashMap<>();
    private final ChannelIdleDetectionConfig idleDetectionConfig = new ChannelIdleDetectionConfig(IDLE_DETECTION_INTERVAL, "!\r");
    private int zoneMapInquiryCount = 0;
    private long lastCheckIn;

    public RadioRaPlugin(String pluginId) {
        super(pluginId);
    }

    @Override
    public String getName() {
        return "Lutron RadioRa Plugin";
    }

    public int getDeviceCount() {
        return devices.size();
    }

    public void sendZoneMapInquiry() {
        zoneMapInquiryCount = 1;
        send(new ZoneMapInquiry());
    }

    public void setSwitchLevel(int zoneNum, boolean isOn) {
        send(new SetSwitchLevel(zoneNum, isOn));
    }

    public void setDimmerLevel(int zoneNum, int level) {
        send(new SetDimmerLevel(zoneNum, level));
    }

    @Override
    protected ChannelInboundHandlerAdapter getDecoder() {
        return new RadioRaFrameDecoder();
    }

    @Override
    protected ChannelOutboundHandlerAdapter getEncoder() {
        return new RadioRaFrameEncoder();
    }

    @Override
    protected ChannelIdleDetectionConfig getIdleDetectionConfig() {
        return idleDetectionConfig;
    }

    @Override
    protected void configureChannel(ChannelConfig cfg) {
        if (cfg instanceof RxtxChannelConfig) {
            RxtxChannelConfig config = (RxtxChannelConfig)cfg;
            config.setBaudrate(9600);
            config.setDatabits(RxtxChannelConfig.Databits.DATABITS_8);
            config.setStopbits(RxtxChannelConfig.Stopbits.STOPBITS_1);
            config.setParitybit(RxtxChannelConfig.Paritybit.NONE);
        }
    }

    @Override
    protected void onChannelConnected() {
        logger.debug("onChannelConnected()");
        sendZoneMapInquiry();
    }

    @Override
    protected void onChannelData(Object o) {
        logger.debug("Received: " + o);
        if (o instanceof LocalZoneChange) {
            onLocalZoneChange((LocalZoneChange) o);
        } else if (o instanceof ZoneMap) {
            onZoneMap((ZoneMap) o);
        } else if (o instanceof LEDMap) {
            onLEDMap((LEDMap)o);
        } else {
            logger.error("Received unknown command: {}", o);
        }
    }

    @Override
    protected void onChannelDisconnected() {
        logger.info("onChannelDisconnected()");

        List<VariableUpdate> updates = new ArrayList<>();
        for (HobsonDevice device : devices.values()) {
            updates.add(
                new VariableUpdate(
                    device.getContext(),
                    VariableConstants.ON,
                    null
                )
            );
        }

        fireVariableUpdateNotifications(updates);
    }

    protected void onLocalZoneChange(LocalZoneChange lzc) {
        logger.debug("onLocalZoneChange: {}", lzc);
        VariableUpdate update = new VariableUpdate(
            DeviceContext.create(
                    getContext(),
                    Integer.toString(lzc.getZoneNumber())
            ),
            VariableConstants.ON,
            lzc.getState() != LocalZoneChange.State.OFF
        );
        fireVariableUpdateNotification(update);
    }

    protected void onZoneMap(ZoneMap zoneMap) {
        logger.debug("onZoneMap: {}", zoneMap);

        long now = System.currentTimeMillis();

        zoneMapInquiryCount = 0;

        // a zone map should always be 32 characters long
        String state = zoneMap.getZoneMap();
        if (state != null && state.length() == 32) {
            List<VariableUpdate> updates = new ArrayList<>();
            for (int i = 0; i < 32; i++) {
                char c = state.charAt(i);
                int zoneId = i + 1;
                if (c == '1' || c == '0') {
                    RadioRaDevice device = devices.get(zoneId);

                    // if we haven't published this device before, do so
                    if (device == null) {
                        device = new RadioRaDevice(this, zoneId, c == '1');
                        publishDevice(device);
                        devices.put(zoneId, device);
                    // otherwise, determine if we should publish a variable update
                    } else {
                        boolean value = (c == '1');

                        // if the device has been started (and therefore it's variables have been published), send an update
                        if (device.isStarted()) {
                            updates.add(
                                new VariableUpdate(
                                    DeviceContext.create(
                                        getContext(),
                                        Integer.toString(zoneId)
                                    ),
                                    VariableConstants.ON,
                                    value
                                )
                            );
                        // otherwise, simply update its initial value so it will be published with the correct startup value
                        } else {
                            device.setStartupValue(value);
                        }
                    }
                } else if (c == 'X') {
                    if (devices.containsKey(zoneId)) {
                        unpublishDevice(Integer.toString(zoneId));
                        devices.remove(zoneId);
                    }
                }
            }
            fireVariableUpdateNotifications(updates);
        } else {
            logger.error("Received invalid zone map: {}", state);
        }
    }

    protected void onLEDMap(LEDMap o) {
        logger.debug("onLEDMap: {}", o);
    }

    @Override
    public long getRefreshInterval() {
        return 5;
    }

    @Override
    public void onRefresh() {
        // if we didn't receive a response to a zone inquiry request, try sending another
        if (zoneMapInquiryCount > 0) {
            if (zoneMapInquiryCount < 3) {
                logger.debug("No response to last zone inquiry detected; sending another");
                send(new ZoneMapInquiry());
                zoneMapInquiryCount++;
            } else {
                logger.error("Giving up on zone inquiry requests; no response detected after 3 tries");
                zoneMapInquiryCount = 0;
            }
        }

        // if the channel is actively connected, perform check-in on all devices
        if (isConnected()) {
            long now = System.currentTimeMillis();
            if (now - lastCheckIn > IDLE_DETECTION_INTERVAL) {
                for (RadioRaDevice d : devices.values()) {
                    d.checkInDevice(now);
                }
                lastCheckIn = now;
            }
        }
    }

    @Override
    protected TypedProperty[] createSupportedProperties() {
        return new TypedProperty[] {
            new TypedProperty.Builder("serial.port", "Serial Port", "The serial port that the Lutron RA-RS232 controller is connected to (should not be used with Serial Hostname)", TypedProperty.Type.STRING).build(),
            new TypedProperty.Builder("serial.hostname", "Serial Hostname", "The hostname of the GlobalCache device that the Lutron RA-RS232 controller is connected to (should not be used with Serial Port)", TypedProperty.Type.STRING).build()
        };
    }
}
