package org.sgrewritten.stargatemapper.hook;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import org.bukkit.Location;
import org.bukkit.World;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargatemapper.DescriptionBuilder;
import org.sgrewritten.stargatemapper.Icon;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.logging.Logger;

public class BluemapHook implements MapperHook {

    private final Logger logger;
    private final MarkerSet markerSet;
    private POIMarker portalMarker;
    private final BlueMapAPI api = BlueMapAPI.getInstance().get();
    private final Collection<BlueMapWorld> worlds = api.getWorlds();

    public BluemapHook(Logger logger) {
        this.logger = logger;
        markerSet = MarkerSet.builder()
                .label("Stargate")
                .build();

        for (BlueMapWorld world : worlds) {
            for (BlueMapMap map : world.getMaps()) {
                map.getMarkerSets().put("Stargate", markerSet);
            }
        }
    }

    @Override
    public void addPortalMarker(RealPortal portal) {
        if (portal.hasFlag(PortalFlag.HIDDEN)) {
            return;
        }

        Location location = portal.getExit();
        World world = portal.getExit().getWorld();
        if (world == null) {
            return;
        }
        portalMarker = POIMarker.builder()
                .label(portal.getName())
                .position(location.getX(), location.getY(), location.getZ())
                .build();

        portalMarker.setIcon(Icon.fromPortal(portal).name(), 10, 10);
        portalMarker.setDetail(DescriptionBuilder.createDescription(portal));

        markerSet.getMarkers().put(portal.getGlobalId().toString(), portalMarker);
    }

    @Override
    public void addPortalMarkers(Collection<Portal> portals) {
        portals.forEach(portal -> {
            if (portal instanceof RealPortal) {
                addPortalMarker((RealPortal) portal);
            }
        });
    }

    @Override
    public void removePortalMarker(RealPortal portal) {
        markerSet.remove(portal.getGlobalId().toString());
    }

    @Override
    public void deleteMarkerSet() {
        // probably not needed
    }

    @Override
    public void registerIcon(BufferedImage image, String key, String type, String title) {
        // TODO: This?
    }

}
