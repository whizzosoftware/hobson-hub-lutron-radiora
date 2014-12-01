/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.api.driver;

import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.api.variable.VariableManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MockVariableManager implements VariableManager {
    private List<VariableUpdate> updates = new ArrayList<>();

    @Override
    public void publishGlobalVariable(String userId, String hubId, String pluginId, HobsonVariable var) {

    }

    @Override
    public void publishDeviceVariable(String userId, String hubId, String s, String s2, HobsonVariable hobsonVariable) {

    }

    @Override
    public void unpublishGlobalVariable(String userId, String hubId, String pluginId, String name) {

    }

    @Override
    public void unpublishAllPluginVariables(String userId, String hubId, String pluginId) {

    }

    @Override
    public void unpublishDeviceVariable(String userId, String hubId, String pluginId, String deviceId, String varName) {

    }

    @Override
    public void unpublishAllDeviceVariables(String userId, String hubId, String pluginId, String deviceId) {

    }

    @Override
    public Collection<HobsonVariable> getGlobalVariables(String userId, String hubId) {
        return null;
    }

    @Override
    public HobsonVariable getGlobalVariable(String userId, String hubId, String name) {
        return null;
    }

    @Override
    public Collection<HobsonVariable> getDeviceVariables(String userId, String hubId, String pluginId, String deviceId) {
        return null;
    }

    @Override
    public HobsonVariable getDeviceVariable(String userId, String hubId, String driverId, String deviceId, String name) {
        return null;
    }

    @Override
    public boolean hasDeviceVariable(String userId, String hubId, String pluginId, String deviceId, String name) {
        return false;
    }

    @Override
    public Long setDeviceVariable(String userId, String hubId, String pluginId, String deviceId, String name, Object value) {
        return null;
    }

    @Override
    public void fireVariableUpdateNotification(String userId, String hubId, HobsonPlugin plugin, VariableUpdate update) {
        updates.add(update);
    }

    @Override
    public void fireVariableUpdateNotifications(String userId, String hubId, HobsonPlugin plugin, List<VariableUpdate> updates) {
        this.updates.addAll(updates);
    }

    public void clearUpdates() {
        updates.clear();
    }

    public List<VariableUpdate> getUpdates() {
        return updates;
    }
}