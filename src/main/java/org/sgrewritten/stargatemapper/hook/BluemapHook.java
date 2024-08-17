package org.sgrewritten.stargatemapper.hook;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;
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
import org.sgrewritten.stargatemapper.StargateMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class BluemapHook implements MapperHook {

    private final Logger logger;
    private final Map<BlueMapWorld, MarkerSet> markerSets = new HashMap<>();
    private static final Pattern WORLD_RE = Pattern.compile("^(.*)#");
    private POIMarker portalMarker;
    private final ConcurrentLinkedQueue<QueuedPortal> portalQueue = new ConcurrentLinkedQueue<>();

    public BluemapHook(Logger logger, StargateMapper stargateMapper) {
        this.logger = logger;

        BlueMapAPI.onEnable(blueMapAPI -> {
            blueMapAPI.getWorlds().forEach(blueMapWorld -> {
                MarkerSet markerSet = MarkerSet.builder()
                        .label("Stargate")
                        .build();
                markerSets.put(blueMapWorld, markerSet);
                blueMapWorld.getMaps().forEach(map -> map.getMarkerSets().put("stargate", markerSet));

            });
            try (InputStream inputStream = StargateMapper.class.getResourceAsStream("/stargate.css")) {
                writeToWebApp(inputStream, "stargate.css", blueMapAPI);
                blueMapAPI.getWebApp().registerStyle("assets/stargate.css");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bukkit.getScheduler().runTask(stargateMapper, () -> {
                // In case blue map api disables instantly
                if (BlueMapAPI.getInstance().isEmpty()) {
                    return;
                }
                QueuedPortal queuedPortal = portalQueue.poll();
                while (queuedPortal != null) {
                    if (queuedPortal.create()) {
                        addPortalMarker(queuedPortal.portal, blueMapAPI);
                    } else {
                        removePortalMarker(queuedPortal.portal, blueMapAPI);
                    }
                    queuedPortal = portalQueue.poll();
                }
            });
        });

        BlueMapAPI.onDisable(blueMapAPI -> {
            stargateMapper.getAllPortals().stream()
                    .filter(portal -> portal instanceof RealPortal)
                    .map(portal -> (RealPortal) portal)
                    .map(portal -> new QueuedPortal(portal, true))
                    .forEach(portalQueue::add);
            markerSets.values().stream()
                    .map(MarkerSet::getMarkers)
                    .forEach(Map::clear);
        });
    }

    @Override
    public void addPortalMarker(RealPortal portal) {
        if (portal.hasFlag(PortalFlag.HIDDEN)) {
            return;
        }
        Optional<BlueMapAPI> blueMapAPIOptional = BlueMapAPI.getInstance();
        blueMapAPIOptional.ifPresentOrElse(blueMapAPI -> addPortalMarker(portal, blueMapAPI),
                () -> portalQueue.add(new QueuedPortal(portal, true))
        );
    }

    private void addPortalMarker(RealPortal portal, BlueMapAPI blueMapAPI) {
        Location location = portal.getExit();
        World world = portal.getExit().getWorld();
        if (world == null) {
            return;
        }
        portalMarker = POIMarker.builder()
                .label(portal.getName())
                .position(location.getX(), location.getY(), location.getZ())
                .build();
        logger.warning(Icon.fromPortal(portal).getFileName());
        portalMarker.setIcon("assets" + Icon.fromPortal(portal).getFileName(), 1, 1);
        portalMarker.setDetail(DescriptionBuilder.createDescription(portal));
        portalMarker.addStyleClasses("portal-marker");

        blueMapAPI.getWorld(location.getWorld())
                .map(markerSets::get)
                .map(MarkerSet::getMarkers)
                .ifPresentOrElse(markers -> markers.put(portal.getGlobalId().toString(), portalMarker),
                        () -> logger.warning("Could not find blue map world representation of world: " + location.getWorld().getName())
                );
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
        Optional<BlueMapAPI> blueMapAPIOptional = BlueMapAPI.getInstance();
        blueMapAPIOptional.ifPresentOrElse(blueMapAPI -> removePortalMarker(portal, blueMapAPI),
                () -> portalQueue.add(new QueuedPortal(portal, false))
        );
    }

    private void removePortalMarker(RealPortal portal, BlueMapAPI blueMapAPI) {
        blueMapAPI.getWorld(portal.getExit().getWorld())
                .map(markerSets::get)
                .ifPresentOrElse(markerSet -> markerSet.remove(portal.getGlobalId().toString()),
                        () -> logger.warning("Could not find blue map world representation of world: " + portal.getExit().getWorld().getName())
                );
    }

    @Override
    public void deleteMarkerSet() {
        // probably not needed
    }

    @Override
    public void registerIcon(InputStream image, Icon key, String type, String title) {
        BlueMapAPI.onEnable(blueMapAPI -> writeToWebApp(image, key.getFileName().substring(1), blueMapAPI));
    }

    private void writeToWebApp(InputStream inputStream, String destination, BlueMapAPI blueMapAPI) {
        try {
            Path path = blueMapAPI.getWebApp().getWebRoot().resolve("assets").resolve(destination);
            Files.createDirectories(path.getParent());
            try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                inputStream.transferTo(outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    record QueuedPortal(RealPortal portal, boolean create) {

    }

}
