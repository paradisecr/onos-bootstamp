package org.onosproject.devicemonitor.cli;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.devicemonitor.api.DeviceMonitorAdminService;
import org.onosproject.devicemonitor.api.DeviceMonitorService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;

import java.util.Map;
import java.util.Set;

/**
 * Commands to get device connect times information and config device monitor parameter.
 */
@Command(scope = "onos", name = "dm",
        description = "Device Monitor")
public class DeviceMonitorCommand extends AbstractShellCommand {

    private DeviceMonitorService deviceMonitorService;

    private DeviceMonitorAdminService deviceMonitorAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Argument(index = 0, name = "p1", description = "param1",
            required = false, multiValued = false)
    String param1 = null;

    @Argument(index = 1, name = "p2", description = "param2",
            required = false, multiValued = false)
    String param2 = null;

    @Argument(index = 2, name = "p3", description = "param3",
            required = false, multiValued = false)
    String param3 = null;

    private final String SET = "set";

    private final String DEVICE = "device";

    private final String FORBIDDENS = "forbiddens";

    private final String MAX = "max";
    @Override
    protected void execute() {
        deviceMonitorService = get(DeviceMonitorService.class);
        deviceMonitorAdminService = get(DeviceMonitorAdminService.class);
        //default:show all data we know
        if (StringUtils.isEmpty(param1)) {
            print("Device Monitor result:");
            print("Device max connect times:%d", deviceMonitorService.getDeviceMaxConnectTimes());
            Set<Map.Entry<DeviceId, Long>> deviceCountSet =  deviceMonitorService.getDeviceCountAll();
            print("Device num:%d", deviceCountSet.size());
            for (Map.Entry<DeviceId, Long> deviceCount : deviceCountSet) {
                print("DeviceId:%s  connnetTimes:%s", deviceCount.getKey(), deviceCount.getValue());
            }
            print("");
            Set<DeviceId> forbiddenSet = deviceMonitorService.getForbiddenDevices();
            print("Forbidden devices list:");
            for (DeviceId deviceId : forbiddenSet) {
                print("DeviceId:%s  connnetTimes:%s", deviceId.toString(), deviceMonitorService.getDeviceCount(deviceId));
            }
            return;
        }

        //get a device connect count record
        if (DEVICE.equalsIgnoreCase(param1)) {
            if (StringUtils.isEmpty(param2)) {
                print("command syntax error,syntax: dm device [DeviceId], like:");
                print("dm device of:0000000000000001");
                return;
            }
            DeviceId deviceId = DeviceId.deviceId(param2);
            long count = deviceMonitorService.getDeviceCount(deviceId);
            print("The device connect times:%d", count);
            return;
        }
        //get forbidden devices
        if (FORBIDDENS.equalsIgnoreCase(param1)) {
            Set<DeviceId> forbiddenSet = deviceMonitorService.getForbiddenDevices();
            print("Forbidden devices list,%d devices:", forbiddenSet.size());
            for (DeviceId deviceId : forbiddenSet) {
                print(deviceId.toString());
            }
            return;
        }
        //get permitted devices max connecte times
        if (MAX.equalsIgnoreCase(param1)) {
            print(String.valueOf(deviceMonitorService.getDeviceMaxConnectTimes()));
            return;
        }
        //set permitted device max connecte times
        if (SET.equalsIgnoreCase(param1)) {
            if (StringUtils.isEmpty(param2) || StringUtils.isEmpty(param3) && !param2.equalsIgnoreCase(SET)) {
                print("command syntax error,syntax: dm set max [num], like:");
                print("dm set max 2");
                return;
            }
            long max = Long.valueOf(param3);
            deviceMonitorAdminService.setDeviceMaxConnectTime(max);
            print("Successfuly set the device max connecte times:%d", max);
            return;
        }
    }
}
