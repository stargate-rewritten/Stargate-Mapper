package org.sgrewritten.stargatemapper.hook;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargatemapper.DescriptionBuilder;
import org.sgrewritten.stargatemapper.Icon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BluemapHook implements MapperHook {

    private final Logger logger;
    private final Map<String, MarkerSet> markerSets = new HashMap<>();
    private static final Pattern WORLD_RE = Pattern.compile("^(.*)#");
    private POIMarker portalMarker;

    public BluemapHook(Logger logger) {
        this.logger = logger;
        for (World world : Bukkit.getWorlds()) {
            markerSets.put(world.getName(), MarkerSet.builder()
                    .label("Stargate")
                    .build());
        }

        BlueMapAPI.onEnable(blueMapAPI -> {
            blueMapAPI.getWorlds().forEach(blueMapWorld -> {
                Matcher matcher = WORLD_RE.matcher(blueMapWorld.getId());
                if (!matcher.find()) {
                    logger.warning("Wrong world id: " + blueMapWorld.getId());
                    return;
                }
                MarkerSet markerSet = markerSets.get(matcher.group(1));
                blueMapWorld.getMaps().forEach(map -> map.getMarkerSets().put("stargate", markerSet));
            });
        });
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
        portalMarker.setIcon("assets" + Icon.fromPortal(portal).getFileName(), 1, 1);
        portalMarker.setDetail(DescriptionBuilder.createDescription(portal));
        
        markerSets.get(world.getName()).getMarkers().put(portal.getGlobalId().toString(), portalMarker);
    }

    @Override
    public void addPortalMarkers(Collection<Portal> portals) {
        portals.forEach(portal -> {
            if (portal instanceof RealPortal realPortal) {
                addPortalMarker(realPortal);
            }
        });
    }

    @Override
    public void removePortalMarker(RealPortal portal) {
        markerSets.get(portal.getExit().getWorld().getName()).remove(portal.getGlobalId().toString());
    }

    @Override
    public void deleteMarkerSet() {
        // probably not needed
    }

    @Override
    public void registerIcon(InputStream image, Icon key, String type, String title) throws IOException {
        BlueMapAPI.onEnable(blueMapAPI -> {
            try {
                Path path = blueMapAPI.getWebApp().getWebRoot().resolve("assets").resolve(key.getFileName().substring(1));
                Files.createDirectories(path.getParent());
                logger.warning(path.toString());
                try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    image.transferTo(outputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
