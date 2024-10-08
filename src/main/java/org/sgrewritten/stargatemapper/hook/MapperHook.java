package org.sgrewritten.stargatemapper.hook;

import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargatemapper.Icon;

import java.io.IOException;
import java.io.InputStream;
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

    void registerIcon(InputStream image, Icon key, String type, String title) throws IOException;
}
