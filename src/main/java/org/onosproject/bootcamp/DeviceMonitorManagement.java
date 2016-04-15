package org.onosproject.bootcamp;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.*;
import org.onlab.util.KryoNamespace;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.*;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by cr on 16-4-15.
 */
@Component(immediate = true)
@Service
public class DeviceMonitorManagement implements DeviceMonitorService, DeviceMonitorAdminService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LogicalClockService clockService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceClockService deviceClockService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceAdminService deviceAdminService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    DistributedSet<DeviceId> forbiddenDevices;

    AtomicCounter maxConnnectTimes;

    private final long DEFAULT_MAX = 10L;

    private DeviceListener deviceListener;

    KryoNamespace.Builder deviceDataSerializer = KryoNamespace.newBuilder().register(KryoNamespaces.API);

    @Activate
    protected void activate() {
        log.info("Started");
        //create data set
        createStorage();
        maxConnnectTimes.set(DEFAULT_MAX);
        //add DeviceListener to DeviceService;
        deviceListener = new InnerDeviceListener();
        deviceService.addListener(deviceListener);
    }

    @Deactivate
    protected void deactivate() {
        //remove DeviceListener from DeviceService
        deviceService.removeListener(deviceListener);
        //clear all data set
        destroyStorage();
        log.info("Stopped");
    }

    private void  createStorage() {
        forbiddenDevices = storageService.<DeviceId>setBuilder()
                .withName("forbidden-devices")
                .withSerializer(Serializer.using(KryoNamespaces.API))
                .build()
                .asDistributedSet();
        maxConnnectTimes = storageService.atomicCounterBuilder()
                .withName("max-con-count")
                .build()
                .asAtomicCounter();
    }

    private void destroyStorage() {
        forbiddenDevices.destroy();
        maxConnnectTimes.destroy();
        for (Device device: deviceService.getDevices()) {
            AtomicCounter count = storageService.atomicCounterBuilder()
                    .withName(device.id().toString())
                    .build()
                    .asAtomicCounter();
            count.destroy();
        }
    }

    private void deviceConnect(Device device) {
        log.info("deviceConnect method is being called.");
        DeviceId deviceId = device.id();
        AtomicCounter prvCount = storageService.atomicCounterBuilder()
                .withName(deviceId.toString())
                .build()
                .asAtomicCounter();
        prvCount.incrementAndGet();
        if (isDorbiddenDevice(deviceId)) {
            handleForbidenConnect(deviceId);
            return;
        }
        if (prvCount.get() >= maxConnnectTimes.get()) {
            forbiddenDevices.add(deviceId);
        }
    }

    private void forbiddDevice(DeviceId deviceId) {
        forbiddenDevices.add(deviceId);
    }


    private void handleForbidenConnect(DeviceId deviceId) {
        deviceAdminService.removeDevice(deviceId);
    }

    @Override
    public Set<Map.Entry<DeviceId, Long>> getDeviceCountAll() {
        Map<DeviceId, Long> deviceCountMap = new HashMap<>();
        for (Device device: deviceService.getDevices()){
            AtomicCounter count = storageService.atomicCounterBuilder()
                    .withName(device.id().toString())
                    .build()
                    .asAtomicCounter();
            deviceCountMap.put(device.id(), count.get());
        }
        return deviceCountMap.entrySet();
    }

    @Override
    public Long getDeviceCount(DeviceId deviceId) {
        AtomicCounter count = storageService.atomicCounterBuilder()
                .withName(deviceId.toString())
                .build()
                .asAtomicCounter();
        return count.get();
    }

    @Override
    public Set<DeviceId> getForbiddenDevices() {
        Set<DeviceId> forbiddenDeviceSet = new HashSet<>();
        for (DeviceId deviceId : forbiddenDevices) {
            forbiddenDeviceSet.add(deviceId);
        }
        return forbiddenDeviceSet;
    }

    @Override
    public long getDeviceMaxConnectTimes() {
        return maxConnnectTimes.get();
    }

    @Override
    public boolean isDorbiddenDevice(DeviceId deviceId) {
        return forbiddenDevices.contains(deviceId);
    }

    @Override
    public void setDeviceMaxConnectTime(long maxTime) {
        maxConnnectTimes.set(maxTime);
        for (DeviceId deviceId : forbiddenDevices) {
            if (getDeviceCount(deviceId) < maxTime) {
                forbiddenDevices.remove(deviceId);
            }
        }
        for (Map.Entry<DeviceId, Long> deviceCountMap : getDeviceCountAll()) {
            if (getDeviceCount(deviceCountMap.getKey()) < maxConnnectTimes.get()) {
                forbiddenDevices.remove(deviceCountMap.getKey());
            } else if (getDeviceCount(deviceCountMap.getKey()) < maxConnnectTimes.get()) {
                forbiddenDevices.add(deviceCountMap.getKey());
            }
        }
    }

    @Override
    public void setDeviceConnectCount(DeviceId deviceId, long count) {
        AtomicCounter prvCount = storageService.atomicCounterBuilder()
                .withName(deviceId.toString())
                .build()
                .asAtomicCounter();
        if (isDorbiddenDevice(deviceId) && prvCount.get() < count) {
            forbiddenDevices.remove(deviceId);
        }
        prvCount.set(count);
    }

    private class InnerDeviceListener implements DeviceListener {

        @Override
        public void event(DeviceEvent deviceEvent) {
            //if device_added add count
            log.info("Receive DeviceEvent,type:{}", deviceEvent.type());
            //Only we are master of the device, we handle it
            DeviceId deviceId = deviceEvent.subject().id();
            if (!mastershipService.isLocalMaster(deviceId)) {
                return;
            }
            switch (deviceEvent.type()) {
                case DEVICE_ADDED:
                    deviceConnect(deviceEvent.subject());
                    break;
                case DEVICE_AVAILABILITY_CHANGED:
                    if (deviceService.isAvailable(deviceId)) {
                        deviceConnect(deviceEvent.subject());
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
