/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.flicbutton.internal.discovery;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.flicbutton.FlicButtonBindingConstants;
import org.openhab.binding.flicbutton.handler.FlicDaemonBridgeHandler;
import org.openhab.binding.flicbutton.internal.FlicButtonHandlerFactory;
import org.openhab.binding.flicbutton.internal.util.FlicButtonUtils;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.flic.fliclib.javaclient.Bdaddr;
import io.flic.fliclib.javaclient.GeneralCallbacks;
import io.flic.fliclib.javaclient.GetInfoResponseCallback;
import io.flic.fliclib.javaclient.enums.BdAddrType;
import io.flic.fliclib.javaclient.enums.BluetoothControllerState;

/**
 * For each configured flicd service, there is a {@link FlicSimpleclientDiscoveryServiceImpl} which will be initialized
 * by
 * {@link FlicButtonHandlerFactory}.
 *
 * It can scan for Flic Buttons already that are already added to fliclib-linux-hci ("verified" buttons), *
 * but it does not support adding and verify new buttons on it's own.
 * New buttons have to be added (verified) e.g. via simpleclient by Shortcut Labs.
 * Background discovery listens for new buttons that are getting verified.
 *
 * @author Patrick Fink - Initial contribution
 */
public class FlicSimpleclientDiscoveryServiceImpl extends AbstractDiscoveryService
        implements FlicButtonDiscoveryService, ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(FlicSimpleclientDiscoveryServiceImpl.class);

    private boolean activated = false;
    private FlicDaemonBridgeHandler bridgeHandler;

    public FlicSimpleclientDiscoveryServiceImpl() {
        super(FlicButtonBindingConstants.SUPPORTED_THING_TYPES_UIDS, 2, true);
    }

    @Override
    public void activate() {
        activated = true;
        super.activate(null);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof FlicDaemonBridgeHandler) {
            bridgeHandler = (FlicDaemonBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void deactivate() {
        activated = false;
        super.deactivate();
    }

    @Override
    protected void startScan() {
        try {
            if (activated) {
                discoverVerifiedButtons();
            }

        } catch (IOException e) {
            logger.warn("Error occured during button discovery", e);
            if (this.scanListener != null) {
                scanListener.onErrorOccurred(e);
            }
        }
    }

    protected void discoverVerifiedButtons() throws IOException {
        // Register FlicButtonEventListener to all already existing Flic buttons
        bridgeHandler.getFlicClient().getInfo(new GetInfoResponseCallback() {
            @Override
            public void onGetInfoResponse(BluetoothControllerState bluetoothControllerState, Bdaddr myBdAddr,
                    BdAddrType myBdAddrType, int maxPendingConnections, int maxConcurrentlyConnectedButtons,
                    int currentPendingConnections, boolean currentlyNoSpaceForNewConnection, Bdaddr[] verifiedButtons)
                    throws IOException {

                for (final Bdaddr bdaddr : verifiedButtons) {
                    flicButtonDiscovered(bdaddr);
                }
            }
        });
    }

    @Override
    protected void startBackgroundDiscovery() {
        super.startBackgroundDiscovery();
        // logger.info(bridgeHandler.toString());
        // logger.info(bridgeHandler.getFlicClient().toString());
        bridgeHandler.getFlicClient().setGeneralCallbacks(new GeneralCallbacks() {
            @Override
            public void onNewVerifiedButton(Bdaddr bdaddr) throws IOException {
                logger.info("A new Flic button was added by an external flicd client: {}", bdaddr);
                flicButtonDiscovered(bdaddr);
            }
        });
    }

    @Override
    protected void stopBackgroundDiscovery() {
        super.stopBackgroundDiscovery();
        if (bridgeHandler.getFlicClient() != null) {
            bridgeHandler.getFlicClient().setGeneralCallbacks(null);
        }
    }

    @Override
    public ThingUID flicButtonDiscovered(Bdaddr bdaddr) {
        logger.info("Flic Button {} discovered!", bdaddr);
        ThingUID flicButtonUID = FlicButtonUtils.getThingUIDFromBdAddr(bdaddr, bridgeHandler.getThing().getUID());

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(flicButtonUID)
                .withBridge(bridgeHandler.getThing().getUID())
                .withLabel("Flic Button " + bdaddr.toString().replace(":", ""))
                .withProperty(FlicButtonBindingConstants.CONFIG_ADDRESS, bdaddr.toString())
                .withRepresentationProperty(FlicButtonBindingConstants.CONFIG_ADDRESS).build();
        this.thingDiscovered(discoveryResult);
        return flicButtonUID;
    }
}
