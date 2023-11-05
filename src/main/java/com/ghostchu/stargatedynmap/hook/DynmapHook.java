package com.ghostchu.stargatedynmap.hook;

import com.ghostchu.stargatedynmap.DescriptionBuilder;
import com.ghostchu.stargatedynmap.Icon;
import com.ghostchu.stargatedynmap.Id;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.PortalFlag;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DynmapHook implements MapperHook {

    private final Logger logger;
    MarkerSet markerSet;
    DynmapAPI dynmapAPI;

    public DynmapHook(PluginManager pluginManager, Logger logger){
        dynmapAPI = (DynmapAPI) pluginManager.getPlugin("dynmap");
        if(dynmapAPI == null){
            throw new IllegalStateException("Could not find the dynmap api.");
        }
        markerSet = dynmapAPI.getMarkerAPI().createMarkerSet("stargate", "Stargate", null, false);
        this.logger = logger;
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
        MarkerIcon portalIcon = dynmapAPI.getMarkerAPI().getMarkerIcon(Icon.fromPortal(portal).name());
        Marker marker = markerSet.createMarker(Id.getPortalMarkerId(portal),
                portal.getName(), world.getName(), location.getX(), location.getY(), location.getZ(), portalIcon, false);
        if (marker == null) {
            logger.log(Level.WARNING, "Unable to create marker for portal " + portal.getName() + " at " +
                    location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockX() + " on network " +
                    portal.getNetwork().getName() + " with destination " + portal.getDestination());
            return;
        }
        String markerDescription = DescriptionBuilder.createDescription(portal);
        marker.setDescription(markerDescription);
        marker.setLabel(portal.getName(), true);
        marker.setMarkerIcon(portalIcon);
    }

    @Override
    public void addPortalMarkers(Collection<Portal> portals) {
        // Delete all markers
        markerSet.getMarkers().forEach(GenericMarker::deleteMarker);
        portals.forEach(portal -> {
            if (portal instanceof RealPortal) {
                addPortalMarker((RealPortal) portal);
            }
        });
    }

    @Override
    public void removePortalMarker(RealPortal portal) {
        Marker marker = markerSet.findMarker(Id.getPortalMarkerId(portal));
        if (marker != null) {
            marker.deleteMarker();
        }
    }

    @Override
    public void deleteMarkerSet() {
        markerSet.deleteMarkerSet();
    }

    @Override
    public void registerIcon(BufferedImage image, String key, String type, String title) {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, type, outputStream);
            try(InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray())){
                dynmapAPI.getMarkerAPI().createMarkerIcon(key, title, inputStream);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not register marker icon for dynmap");
        }
    }

}
