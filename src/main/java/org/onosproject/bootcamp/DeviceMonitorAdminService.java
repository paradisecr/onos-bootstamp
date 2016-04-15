package org.onosproject.bootcamp;

import org.onosproject.net.DeviceId;

/**
 * Created by cr on 16-4-15.
 */
public interface DeviceMonitorAdminService {
    void setDeviceMaxConnectTime(long maxTime);
//    void addDevice2WhiteList(DeviceId deviceId);
//    void removeDeviceFromWhiteList(DeviceId deviceId);
    void setDeviceConnectCount(DeviceId deviceId, long count);
}
