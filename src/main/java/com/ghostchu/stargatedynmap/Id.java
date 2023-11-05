package com.ghostchu.stargatedynmap;

import org.sgrewritten.stargate.api.network.portal.Portal;

public class Id {



    /**
     * Gets the marker id to use for the given portal
     *
     * @param portal <p>The portal to get a marker id for</p>
     * @return <p>The marker id to use</p>
     */
    public static String getPortalMarkerId(Portal portal) {
        return portal.getNetwork().getId() + ":" + portal.getName();
    }
}
