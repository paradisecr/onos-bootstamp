package org.onosproject.devicemonitor.api;

import org.onosproject.net.DeviceId;

import java.util.Map;
import java.util.Set;

/**
 * Service for interacting with the device monitor module to get device up/down times.
 */
public interface DeviceMonitorService {
    Set<Map.Entry<DeviceId, Long>> getDeviceCountAll();
    Long getDeviceCount(DeviceId deviceId);
    Set<DeviceId> getForbiddenDevices();
    long getDeviceMaxConnectTimes();
    boolean isDorbiddenDevice(DeviceId deviceId);
}
