package com.whizzosoftware.hobson.api.device.manager;

import com.whizzosoftware.hobson.api.config.Configuration;
import com.whizzosoftware.hobson.api.device.DeviceConfigurationListener;
import com.whizzosoftware.hobson.api.device.DeviceManager;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;

import java.util.Collection;

public class MockDeviceManager implements DeviceManager {
    @Override
    public void publishDevice(String userId, String hubId, HobsonPlugin plugin, HobsonDevice device) {

    }

    @Override
    public Collection<HobsonDevice> getAllDevices(String userId, String hubId) {
        return null;
    }

    @Override
    public Collection<HobsonDevice> getAllPluginDevices(String userId, String hubId, String pluginId) {
        return null;
    }

    @Override
    public HobsonDevice getDevice(String userId, String hubId, String pluginId, String deviceId) {
        return null;
    }

    @Override
    public boolean hasDevice(String userId, String hubId, String pluginId, String deviceId) {
        return false;
    }

    @Override
    public Configuration getDeviceConfiguration(String userId, String hubId, String pluginId, String deviceId) {
        return null;
    }

    @Override
    public void setDeviceConfigurationProperty(String userId, String hubId, String pluginId, String deviceId, String name, Object value, boolean overwrite) {

    }

    @Override
    public void setDeviceName(String userId, String hubId, String pluginId, String deviceId, String name) {

    }

    @Override
    public void unpublishDevice(String userId, String hubId, HobsonPlugin plugin, String deviceId) {

    }

    @Override
    public void unpublishAllDevices(String userId, String hubId, HobsonPlugin plugin) {

    }

    @Override
    public void registerForDeviceConfigurationUpdates(String userId, String hubId, String pluginId, String deviceId, DeviceConfigurationListener listener) {

    }
}
