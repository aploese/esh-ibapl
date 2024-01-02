/*
 * ESH-IBAPL  - OpenHAB bindings for various IB APL drivers, https://github.com/aploese/esh-ibapl/
 * Copyright (C) 2017-2023, Arne Plöse and individual contributors as indicated
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
package de.ibapl.openhab.fhz4j.console;

import de.ibapl.fhz4j.cul.CulRemainingFhtDeviceOutBufferSizeRequest;
import de.ibapl.fhz4j.cul.CulResponse;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openhab.core.io.console.Console;
import org.openhab.core.io.console.extensions.AbstractConsoleCommandExtension;
import org.openhab.core.io.console.extensions.ConsoleCommandExtension;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingManager;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.i18n.ThingStatusInfoI18nLocalizationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import de.ibapl.fhz4j.api.Response;
import de.ibapl.fhz4j.cul.CulFhtDeviceOutBufferContentRequest;
import de.ibapl.fhz4j.cul.CulGetFirmwareVersionRequest;
import de.ibapl.fhz4j.cul.CulGetHardwareVersionRequest;
import de.ibapl.fhz4j.cul.CulGetSlowRfSettingsRequest;

import de.ibapl.openhab.fhz4j.handler.SpswBridgeHandler;
import java.io.IOException;

/**
 * {@link Fhz4JThingConsoleCommandExtension} provides console commands for
 * listing and removing things.
 *
 * @author aploese
 */
@Component(immediate = true, service = ConsoleCommandExtension.class)
public class Fhz4JThingConsoleCommandExtension extends AbstractConsoleCommandExtension {

    private static final String CMD_FHZ4J = "fhz4j";
    private static final String SUBCMD_LIST = "list";
    private static final String SUBCMD_TX_SHOW = "txshow";
    private static final String SUBCMD_TX_CLEAR = "txclear";

    private final ThingRegistry thingRegistry;
    private final ThingStatusInfoI18nLocalizationService thingStatusInfoI18nLocalizationService;
 
    @Activate
    public Fhz4JThingConsoleCommandExtension(
            final @Reference ThingRegistry thingRegistry,
            final @Reference ThingStatusInfoI18nLocalizationService thingStatusInfoI18nLocalizationService) {
        super(CMD_FHZ4J, "Access and mange your fhz4j things.");
        this.thingRegistry = thingRegistry;
        this.thingStatusInfoI18nLocalizationService = thingStatusInfoI18nLocalizationService;
    }

    @Override
    public void execute(String[] args, Console console) {
        if (args.length > 0) {
            String subCommand = args[0];
            switch (subCommand) {
                case SUBCMD_LIST:
                    printThings(console);
                    return;
                case SUBCMD_TX_SHOW:
                    if (args.length > 1) {
                        ThingUID thingUID = new ThingUID(args[1]);
                        txShow(console, thingUID);
                    } else {
                        console.println("Specify fhz4j thing id to show tx buffer: fhz4j txclear <thingUID> (e.g. \"fhz4j:rs232-bridge-cul:CUL0\")");
                    }
                    return;
                case SUBCMD_TX_CLEAR:
                    if (args.length > 1) {
                        ThingUID thingUID = new ThingUID(args[1]);
                        txClear(console, thingUID);
                    } else {
                        console.println("Specify fhz4j thing id to clear tx buffer: fhz4j txclear <thingUID> (e.g. \"fhz4j:rs232-bridge-cul:CUL0\")");
                    }
                    return;
                default:
                    break;
            }
        } else {
            printUsage(console);
        }
    }

    private void txClear(Console console, ThingUID thingUID) {
        final ThingHandler thingHandler = thingRegistry.get(thingUID).getHandler();
        if (thingHandler instanceof SpswBridgeHandler) {
            final SpswBridgeHandler handler = (SpswBridgeHandler) thingHandler;
            try {
                handler.clearFht8bBuffer();
                console.println("tx cleared for thing " + thingUID + ".");
            } catch (IOException ioe) {
                console.println("Could not tx clear thing " + thingUID + "." + ioe);
            }
        } else {
            console.println("Could not tx clear thing " + thingUID + ".");
        }
    }

    private void txShow(Console console, ThingUID thingUID) {
        ThingHandler thingHandler = thingRegistry.get(thingUID).getHandler();
        
        if (thingHandler instanceof SpswBridgeHandler) {
            final SpswBridgeHandler handler = (SpswBridgeHandler) thingHandler;
            try {
                Future<Response> future;
                Response response;

                future = handler.sendRequest(new CulRemainingFhtDeviceOutBufferSizeRequest());
                response = future.get(1, TimeUnit.SECONDS);
                console.println("RemainingFhtDeviceOutBufferSize: " + response);
            } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
                console.println("Could not tx show thing \"" + thingUID + "\". exception: " + e);
            }
        } else {
            console.println("Could not tx show thing \"" + thingUID + "\". handler: " + thingHandler);
        }
    }

    @Override
    public List<String> getUsages() {
        return List.of(buildCommandUsage(SUBCMD_LIST, "lists all things"),
                buildCommandUsage(SUBCMD_TX_SHOW + " <thingUID>", "show tx buffer of a fhz4j thing"),
                buildCommandUsage(SUBCMD_TX_CLEAR + " <thingUID>", "clear tx buffer of a fhz4j thing"));
    }

    private void printThings(Console console) {

        for (Thing thing : thingRegistry.getAll()) {
            if (thing.getHandler() instanceof SpswBridgeHandler) {
                final SpswBridgeHandler handler = (SpswBridgeHandler) thing.getHandler();
                ThingStatusInfo status = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, null);
                String label = thing.getLabel();
                String id = thing.getUID().toString();
                console.println(String.format("%s (Type=Bridge, Status=%s, Label=%s)", id, status, label));
                try {
                    Future<Response> future;
                    Response response;

                    future = handler.sendRequest(new CulGetSlowRfSettingsRequest());
                    response = future.get(1, TimeUnit.SECONDS);
                    console.println("SlowRfSettings: " + response);

                    future = handler.sendRequest(new CulFhtDeviceOutBufferContentRequest());
                    response = future.get(1, TimeUnit.SECONDS);
                    console.println("FhtDeviceOutBufferContent: " + response);

                    future = handler.sendRequest(new CulRemainingFhtDeviceOutBufferSizeRequest());
                    response = future.get(1, TimeUnit.SECONDS);
                    console.println("RemainingFhtDeviceOutBufferSize: " + response);

                    future = handler.sendRequest(new CulGetFirmwareVersionRequest());
                    response = future.get(1, TimeUnit.SECONDS);
                    console.println("RemainingFhtDeviceOutBufferSize: " + response);

                    future = handler.sendRequest(new CulGetHardwareVersionRequest());
                    response = future.get(1, TimeUnit.SECONDS);
                    console.println("RemainingFhtDeviceOutBufferSize: " + response);
                } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
                    console.println("Could not tx show thing " + handler + "." + e);
                }
            }
        }
    }

}
