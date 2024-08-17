package org.sgrewritten.stargatemapper.hook;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.event.EventHandler;
import net.pl3x.map.core.event.EventListener;
import net.pl3x.map.core.event.server.Pl3xMapEnabledEvent;
import net.pl3x.map.core.event.world.WorldLoadedEvent;
import net.pl3x.map.core.event.world.WorldUnloadedEvent;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Popup;
import net.pl3x.map.core.markers.option.Tooltip;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.sgrewritten.stargate.api.gate.GateAPI;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.flag.PortalFlag;
import org.sgrewritten.stargatemapper.DescriptionBuilder;
import org.sgrewritten.stargatemapper.Icon;
import org.sgrewritten.stargatemapper.Id;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;

public class Pl3xmapHook implements MapperHook, EventListener {

    private final Pl3xMap pl3xmapAPI;
    private final static String STARGATE_LAYER = "stargate";

    public Pl3xmapHook(PluginManager pluginManager) {
        this.pl3xmapAPI = Pl3xMap.api();
        this.pl3xmapAPI.getWorldRegistry().forEach(this::registerLayer);
        this.pl3xmapAPI.getEventRegistry().register(this);
    }

    private void registerLayer(net.pl3x.map.core.world.World world) {
        world.getLayerRegistry().register(new SimpleLayer(STARGATE_LAYER, () -> "Stargate portals"));
    }

    @Override
    public void addPortalMarker(RealPortal portal) {
        if (portal.hasFlag(PortalFlag.HIDDEN)) {
            return;
        }

        Location location = portal.getExit();
        String destinationName = portal.getBehavior().getDestinationName();
        String owner = Bukkit.getOfflinePlayer(portal.getOwnerUUID()).getName();
        if (owner == null) {
            owner = portal.getOwnerUUID().toString();
        }
        if (StringUtils.isEmpty(destinationName)) {
            destinationName = "<Non-directional Stargate>";
        }
        World world = portal.getExit().getWorld();
        if (world == null) {
            return;
        }
        GateAPI gate = portal.getGate();
        String id = Id.getPortalMarkerId(portal);
        Point point = new Point(gate.getTopLeft().getBlockX(), gate.getTopLeft().getBlockZ());
        net.pl3x.map.core.markers.marker.Icon marker = new net.pl3x.map.core.markers.marker.Icon(id, point, Icon.fromPortal(portal).name());
        Options options = Options.builder().tooltip(new Tooltip(id)).popup(new Popup(DescriptionBuilder.createDescription(portal))).build();
        marker.setOptions(options);
        net.pl3x.map.core.world.World pl3xmapWorld = pl3xmapAPI.getWorldRegistry().get(Objects.requireNonNull(location.getWorld()).getName());
        SimpleLayer layer = (SimpleLayer) Objects.requireNonNull(pl3xmapWorld.getLayerRegistry().get(STARGATE_LAYER));
        layer.addMarker(marker);
    }

    @Override
    public void addPortalMarkers(Collection<Portal> portals) {
        for (Portal portal : portals) {
            if (portal instanceof RealPortal) {
                addPortalMarker((RealPortal) portal);
            }
        }
    }

    @Override
    public void removePortalMarker(RealPortal portal) {
        net.pl3x.map.core.world.World pl3xmapWorld = pl3xmapAPI.getWorldRegistry().get(Objects.requireNonNull(portal.getExit().getWorld()).getName());
        SimpleLayer layer = (SimpleLayer) Objects.requireNonNull(pl3xmapWorld.getLayerRegistry().get(STARGATE_LAYER));
        layer.removeMarker(Id.getPortalMarkerId(portal));
    }

    @Override
    public void deleteMarkerSet() {
        pl3xmapAPI.getWorldRegistry().forEach((world) -> {
            world.getLayerRegistry().unregister(STARGATE_LAYER);
        });
    }

    @Override
    public void registerIcon(InputStream image, Icon key, String type, String title) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(image));
        this.pl3xmapAPI.getIconRegistry().register(new IconImage(key.name(), bufferedImage, type));
    }

    @EventHandler
    public void onWorldLoaded(WorldLoadedEvent event) {
        registerLayer(event.getWorld());
    }

    @EventHandler
    public void onWorldUnloaded(WorldUnloadedEvent event) {
        event.getWorld().getLayerRegistry().unregister(STARGATE_LAYER);
    }

    @EventHandler
    public void onPl3xMapEnabled(Pl3xMapEnabledEvent event) {
        Pl3xMap.api().getWorldRegistry().forEach(this::registerLayer);
    }
}
