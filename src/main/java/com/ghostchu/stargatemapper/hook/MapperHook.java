package com.ghostchu.stargatemapper.hook;

import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

import java.awt.image.BufferedImage;
import java.util.Collection;

public interface MapperHook {

    /**
     * Adds a marker for the given portal
     *
     * @param portal <p>The portal to add a marker for</p>
     */
    void addPortalMarker(RealPortal portal);

    void addPortalMarkers(Collection<Portal> portals);

    void removePortalMarker(RealPortal portal);

    void deleteMarkerSet();

    void registerIcon(BufferedImage image, String key, String type, String title);
}
