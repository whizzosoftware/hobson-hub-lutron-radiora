package com.whizzosoftware.hobson.api.device.manager;

import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;

import java.util.Collection;

public class MockDeviceManager implements DeviceManager {
    @Override
    public Collection<HobsonDevice> getAllDevices() {
        return null;
    }

    @Override
    public Collection<HobsonDevice> getAllPluginDevices(String pluginId) {
        return null;
    }

    @Override
    public HobsonDevice getDevice(String s, String s2) {
        return null;
    }

    @Override
    public boolean hasDevice(String pluginId, String deviceId) {
        return false;
    }

    @Override
    public void publishDevice(HobsonPlugin plugin, HobsonDevice device) {

    }

    @Override
    public void unpublishAllDevices(HobsonPlugin plugin) {

    }

    @Override
    public void unpublishDevice(HobsonPlugin plugin, String s2) {

    }

    @Override
    public void setDeviceName(String s, String s2, String s3) {

    }
}
