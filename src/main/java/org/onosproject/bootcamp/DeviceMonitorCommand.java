package org.onosproject.bootcamp;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.store.service.AtomicCounter;
import org.onosproject.store.service.DistributedSet;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.StorageService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by cr on 16-4-15.
 */
@Command(scope = "onos", name = "dm",
        description = "Device Monitor")
public class DeviceMonitorCommand extends AbstractShellCommand {

    DeviceMonitorService deviceMonitorService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Argument(index = 0, name = "p1", description = "param1",
            required = false, multiValued = false)
    String param1 = null;

    @Override
    protected void execute() {
        //show all data we know
        if (StringUtils.isEmpty(param1)) {
            deviceMonitorService = get(DeviceMonitorService.class);
            print("Device Monitor result:");
            print("Device max connect times:%d", deviceMonitorService.getDeviceMaxConnectTimes());
            Set<Map.Entry<DeviceId, Long>> deviceCountSet =  deviceMonitorService.getDeviceCountAll();
            print("Device num:%d", deviceCountSet.size());
            for (Map.Entry<DeviceId, Long> deviceCount : deviceCountSet) {
                print("DeviceId:%s  connnetTimes:%s", deviceCount.getKey(), deviceCount.getValue());
            }
            Set<DeviceId> forbidenSet = deviceMonitorService.getForbiddenDevices();
            print("Forbidden devices list:");
            for (DeviceId deviceId : forbidenSet) {
                print(deviceId.toString());
            }
        }

    }
}
