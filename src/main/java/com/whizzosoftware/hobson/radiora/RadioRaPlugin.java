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

import com.whizzosoftware.hobson.api.device.proxy.HobsonDeviceProxy;
import com.whizzosoftware.hobson.api.plugin.channel.AbstractChannelObjectPlugin;
import com.whizzosoftware.hobson.api.plugin.channel.ChannelIdleDetectionConfig;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.radiora.api.codec.RadioRaFrameDecoder;
import com.whizzosoftware.hobson.radiora.api.codec.RadioRaFrameEncoder;
import com.whizzosoftware.hobson.radiora.api.command.*;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.rxtx.RxtxChannelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Hobson driver that controls a Lutron RadioRa system via a RA-RS232 interface.
 *
 * @author Dan Noguerol
 */
public class RadioRaPlugin extends AbstractChannelObjectPlugin {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static int IDLE_DETECTION_INTERVAL = 10;

    private final ChannelIdleDetectionConfig idleDetectionConfig = new ChannelIdleDetectionConfig(IDLE_DETECTION_INTERVAL, "!\r");
    private int zoneMapInquiryCount = 0;
    private long lastCheckIn;

    public RadioRaPlugin(String pluginId, String version, String description) {
        super(pluginId, version, description);
    }

    @Override
    public void onStartup(PropertyContainer config) {
        super.onStartup(config);

        logger.debug("RadioRa plugin has started");
    }

    @Override
    public void onShutdown() {
        super.onShutdown();

        logger.debug("RadioRa plugin has shut down");
    }

    @Override
    public String getName() {
        return "Lutron RadioRa Plugin";
    }

    int getDeviceCount() {
        return getDeviceProxies().size();
    }

    private void sendZoneMapInquiry() {
        zoneMapInquiryCount = 1;
        send(new ZoneMapInquiry());
    }

    void setSwitchLevel(int zoneNum, boolean isOn) {
        send(new SetSwitchLevel(zoneNum, isOn));
    }

    void setDimmerLevel(int zoneNum, int level) {
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

        // flag any current devices as available
        long now = System.currentTimeMillis();
        for (HobsonDeviceProxy device : getDeviceProxies()) {
            device.setLastCheckin(now);
        }

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
        logger.debug("onChannelDisconnected()");

        // flag all devices as unavailable
        for (HobsonDeviceProxy device : getDeviceProxies()) {
            device.setLastCheckin(null);
        }
    }

    void onLocalZoneChange(LocalZoneChange lzc) {
        logger.debug("onLocalZoneChange: {}", lzc);
        String deviceId = Integer.toString(lzc.getZoneNumber());
        if (hasDeviceProxy(deviceId)) {
            RadioRaDevice proxy = (RadioRaDevice) getDeviceProxy(Integer.toString(lzc.getZoneNumber()));
            proxy.onZoneUpdate(lzc.getState() != LocalZoneChange.State.OFF);
        }
    }

    void onZoneMap(ZoneMap zoneMap) {
        onZoneMap(zoneMap, false);
    }

    void onZoneMap(ZoneMap zoneMap, boolean sync) {
        long start = System.currentTimeMillis();
        logger.debug("onZoneMap: {}", zoneMap);

        zoneMapInquiryCount = 0;

        // a zone map should always be 32 characters long
        String state = zoneMap.getZoneMap();
        if (state != null && state.length() == 32) {
            for (int i = 0; i < 32; i++) {
                char c = state.charAt(i);
                int zoneId = i + 1;
                if (c == '1' || c == '0') {
                    String deviceId = Integer.toString(zoneId);
                    // if we haven't published this device before, do so
                    if (!hasDeviceProxy(deviceId)) {
                        logger.debug("Found new zone {}", zoneId);
                        RadioRaDevice device = new RadioRaDevice(this, zoneId, c == '1');
                        if (sync) {
                            publishDeviceProxy(device).syncUninterruptibly();
                        } else {
                            publishDeviceProxy(device);
                        }
                        device.setLastCheckin(System.currentTimeMillis());
                    // otherwise, provide the device its new value
                    } else {
                        RadioRaDevice device = (RadioRaDevice)getDeviceProxy(deviceId);
                        boolean value = (c == '1');
                        logger.debug("Found update for existing zone {}", zoneId);
                        device.onZoneUpdate(value);
                    }
                } else if (c == 'X') {
                    deleteDeviceProxy(Integer.toString(zoneId));
                }
            }
        } else {
            logger.error("Received invalid zone map: {}", state);
        }

        logger.error("onZoneMap processing took {} ms", System.currentTimeMillis() - start);
    }

    private void onLEDMap(LEDMap o) {
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
                for (HobsonDeviceProxy d : getDeviceProxies()) {
                    d.setLastCheckin(now);
                }
                lastCheckIn = now;
            }
        }
    }

    @Override
    protected TypedProperty[] getConfigurationPropertyTypes() {
        return new TypedProperty[] {
            new TypedProperty.Builder(PROP_SERIAL_PORT, "Serial Port", "The serial port that the Lutron RA-RS232 controller is connected to (should not be used with Serial Hostname)", TypedProperty.Type.STRING).build(),
            new TypedProperty.Builder(PROP_SERIAL_HOSTNAME, "Serial Hostname", "The hostname of the GlobalCache device that the Lutron RA-RS232 controller is connected to (should not be used with Serial Port)", TypedProperty.Type.STRING).build()
        };
    }
}
