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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;

import io.flic.fliclib.javaclient.Bdaddr;

/**
 *
 * @author Patrick Fink
 *
 */
@NonNullByDefault
public interface FlicButtonDiscoveryService extends DiscoveryService {

    /**
     *
     * @param bdaddr Bluetooth address of the discovered Flic button
     * @return UID that was created by the discovery service
     */
    public ThingUID flicButtonDiscovered(Bdaddr bdaddr);
}
