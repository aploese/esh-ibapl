/*
 * ESH-IBAPL  - OpenHAB bindings for various IB APL drivers, https://github.com/aploese/esh-ibapl/
 * Copyright (C) 2017-2019, Arne Plöse and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.ibapl.esh.fhz4j.internal.discovery;

import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

import de.ibapl.esh.fhz4j.FHZ4JBindingConstants;
import de.ibapl.esh.fhz4j.handler.SpswBridgeHandler;
import de.ibapl.fhz4j.cul.CulMessage;
import de.ibapl.fhz4j.cul.CulMessageListener;
import de.ibapl.fhz4j.protocol.em.EmMessage;
import de.ibapl.fhz4j.protocol.evohome.EvoHomeDeviceMessage;
import de.ibapl.fhz4j.protocol.evohome.EvoHomeMessage;
import de.ibapl.fhz4j.protocol.evohome.EvoHomeProperty;
import de.ibapl.fhz4j.protocol.fht.FhtMessage;
import de.ibapl.fhz4j.protocol.fs20.FS20Message;
import de.ibapl.fhz4j.protocol.hms.HmsMessage;
import de.ibapl.fhz4j.protocol.lacrosse.tx2.LaCrosseTx2Message;

/**
 *
 * @author aploese@gmx.de - Initial contribution
 */
@NonNullByDefault
public class FHZ4JDiscoveryService extends AbstractDiscoveryService implements CulMessageListener {

    private final Logger logger = Logger.getLogger("esh.binding.fhz4j");

    private final static int SEARCH_TIME = 15 * 60; // 15 minutes FHT80 sends all 120 sec, HMS 100 TF all 10 min.

    private final SpswBridgeHandler spswBridgeHandler;

    public FHZ4JDiscoveryService(SpswBridgeHandler spswBridgeHandler) {
        super(SpswBridgeHandler.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, false);
        this.spswBridgeHandler = spswBridgeHandler;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SpswBridgeHandler.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        spswBridgeHandler.setDiscoveryListener(this);
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        spswBridgeHandler.setDiscoveryListener(null);
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void deactivate() {
        stopScan();
    }

    private void addHms100TfDevice(short housecode) {
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final String deviceIdStr = Short.toString(housecode);
        final ThingUID thingUID = getThingUID(deviceIdStr, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_HMS_100_TF);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_HMS_100_TF).withProperty("housecode", housecode)
                .withBridge(bridgeUID).withRepresentationProperty(Short.toString(housecode))
                .withLabel("HMS 100 TF " + housecode).build();

        thingDiscovered(discoveryResult);
    }

    private void addEm1000EmDevice(short address) {
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final String deviceIdStr = Short.toString(address);
        final ThingUID thingUID = getThingUID(deviceIdStr, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_EM_1000_EM);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_EM_1000_EM).withProperty("address", address)
                .withBridge(bridgeUID).withRepresentationProperty(Short.toString(address))
                .withLabel("EM 1000 EM " + address).build();

        thingDiscovered(discoveryResult);
    }

    private void addRadiatorEvoHomeDevice(int deviceId) {
        final String hexDeviceId = String.format("%06x", deviceId);
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final ThingUID thingUID = getThingUID(hexDeviceId, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_EVO_HOME);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_EVO_HOME)
                .withProperty("deviceId", deviceId).withBridge(bridgeUID)
                .withRepresentationProperty(hexDeviceId).withLabel("EvoHome Radiator 0x" + hexDeviceId).build();

        thingDiscovered(discoveryResult);
    }

    private void addSingleZoneThermostatEvoHomeDevice(int deviceId) {
        final String hexDeviceId = String.format("%06x", deviceId);
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final ThingUID thingUID = getThingUID(hexDeviceId, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_SINGLE_ZONE_THERMOSTAT_EVO_HOME);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_SINGLE_ZONE_THERMOSTAT_EVO_HOME)
                .withProperty("deviceId", deviceId).withBridge(bridgeUID)
                .withRepresentationProperty(hexDeviceId).withLabel("EvoHome Single Zone Thermostat 0x" + hexDeviceId).build();

        thingDiscovered(discoveryResult);
    }

    private void addMultiZoneControllerEvoHomeDevice(int deviceId) {
        final String hexDeviceId = String.format("%06x", deviceId);
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final ThingUID thingUID = getThingUID(hexDeviceId, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_MULTI_ZONE_CONTROLLER_EVO_HOME);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_MULTI_ZONE_CONTROLLER_EVO_HOME)
                .withProperty("deviceId", deviceId).withBridge(bridgeUID)
                .withRepresentationProperty(hexDeviceId).withLabel("EvoHome Multi Zone Controller 0x" + hexDeviceId).build();

        thingDiscovered(discoveryResult);
    }

    private void addFhtDevice(short housecode) {
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final String deviceIdStr = Short.toString(housecode);
        final ThingUID thingUID = getThingUID(deviceIdStr, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_FHT80B);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_FHT80B)
                .withProperty("housecode", housecode).withBridge(bridgeUID)
                .withRepresentationProperty(Short.toString(housecode)).withLabel("FHT80b " + housecode).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID getThingUID(String deviceId, ThingUID bridgeUID, ThingTypeUID thingTypeUID) {
        return new ThingUID(thingTypeUID, bridgeUID, deviceId);
    }

    @Override
    public void emDataParsed(@Nullable EmMessage emMsg) {
        switch (emMsg.emDeviceType) {
            case EM_1000_EM:
                addEm1000EmDevice(emMsg.address);
                break;
            default:
                throw new RuntimeException("EM 1000 TYpe " + emMsg.emDeviceType + " not implemented yet");
        }
    }

    @Override
    public void failed(@Nullable Throwable arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fhtDataParsed(@Nullable FhtMessage fhtMsg) {
        addFhtDevice(fhtMsg.housecode);
    }

    @Override
    public void fhtPartialDataParsed(@Nullable FhtMessage arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fs20DataParsed(@Nullable FS20Message arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void hmsDataParsed(@Nullable HmsMessage hmsMsg) {
        switch (hmsMsg.hmsDeviceType) {
            case HMS_100_TF:
                addHms100TfDevice(hmsMsg.housecode);
                break;
            default:
                throw new RuntimeException("HMS TYpe " + hmsMsg.hmsDeviceType + " not implemented yet");
        }
    }

    @Override
    public void laCrosseTxParsed(@Nullable LaCrosseTx2Message arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void culMessageParsed(@Nullable CulMessage arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void evoHomeParsed(EvoHomeMessage evoHomeMsg) {
        if (evoHomeMsg instanceof EvoHomeDeviceMessage) {
            final EvoHomeDeviceMessage devMsg = (EvoHomeDeviceMessage) evoHomeMsg;
            switch (devMsg.deviceId1.type) {
                case MULTI_ZONE_CONTROLLER:
                    addMultiZoneControllerEvoHomeDevice(devMsg.deviceId1.id);
                    break;
                case SINGLE_ZONE_THERMOSTAT:
                    addSingleZoneThermostatEvoHomeDevice(devMsg.deviceId1.id);
                    break;
                case RADIATOR_CONTROLLER:
                    addRadiatorEvoHomeDevice(devMsg.deviceId1.id);
                    break;
                default:
                    logger.severe("Cant handle EvoHomeDeviceMessage: " + devMsg);
            }
        }
    }

    @Override
    public void signalStrength(float signalStrength) {
        // TODO Auto-generated method stub
    }

}
