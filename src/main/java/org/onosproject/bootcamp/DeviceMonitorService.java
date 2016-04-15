package org.onosproject.bootcamp;

import org.onosproject.net.DeviceId;

import java.util.Map;
import java.util.Set;

/**
 * Created by cr on 16-4-15.
 */
public interface DeviceMonitorService {
    Set<Map.Entry<DeviceId, Long>> getDeviceCountAll();
    Long getDeviceCount(DeviceId deviceId);
    Set<DeviceId> getForbiddenDevices();
//    Set<DeviceId> getWhiteDevices();
    long getDeviceMaxConnectTimes();
    boolean isDorbiddenDevice(DeviceId deviceId);
}
