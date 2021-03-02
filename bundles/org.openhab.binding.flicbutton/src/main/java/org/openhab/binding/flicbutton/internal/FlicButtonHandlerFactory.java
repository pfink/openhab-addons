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
package org.openhab.binding.flicbutton.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.flicbutton.FlicButtonBindingConstants;
import org.openhab.binding.flicbutton.handler.FlicButtonHandler;
import org.openhab.binding.flicbutton.handler.FlicDaemonBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link FlicButtonHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrick Fink - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.flicbutton")
public class FlicButtonHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(FlicButtonBindingConstants.BRIDGE_THING_TYPES_UIDS.stream(),
                    FlicButtonBindingConstants.SUPPORTED_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(FlicButtonBindingConstants.FLICBUTTON_THING_TYPE)) {
            return new FlicButtonHandler(thing);
        } else if (thingTypeUID.equals(FlicButtonBindingConstants.BRIDGE_THING_TYPE)) {
            FlicDaemonBridgeHandler bridgeHandler = new FlicDaemonBridgeHandler((Bridge) thing);
            return bridgeHandler;
        }

        return null;
    }
}
