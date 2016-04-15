package org.onosproject.devicemonitor.api;

import org.onosproject.net.DeviceId;

/**
 * Service for administering the device monitor module.
 */
public interface DeviceMonitorAdminService {
    /**
     * Set the permitted max connect times for device.
     * @param maxTime
     */
    void setDeviceMaxConnectTime(long maxTime);

    /**
     *Set a device's connect times a new value.
     * @param deviceId
     * @param count
     */
    void setDeviceConnectCount(DeviceId deviceId, long count);
}
