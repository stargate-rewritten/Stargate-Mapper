package org.sgrewritten.stargatemapper.hook;

import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargatemapper.DescriptionBuilder;
import org.sgrewritten.stargatemapper.Icon;
import org.sgrewritten.stargatemapper.Id;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;

public class SquaremapHook implements MapperHook {
    private final Squaremap squaremapAPI;
    private final static Key STARGATE_LAYER = Key.of("stargate");

    public SquaremapHook(PluginManager manager) {
        this.squaremapAPI = SquaremapProvider.get();
        squaremapAPI.mapWorlds().forEach(this::registerLayer);
    }

    private void registerLayer(MapWorld world) {
        SimpleLayerProvider provider = SimpleLayerProvider.builder("Layer Label")
                .showControls(true)
                .defaultHidden(false)
                .layerPriority(5)
                .zIndex(250)
                .build();
        world.layerRegistry().register(STARGATE_LAYER, provider);
    }

    @Override
    public void addPortalMarker(RealPortal portal) {
        World world = Objects.requireNonNull(portal.getExit().getWorld());
        MapWorld mapWorld = squaremapAPI.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).orElse(null);

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
        World world = Objects.requireNonNull(portal.getExit().getWorld());
        MapWorld mapWorld = squaremapAPI.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).orElse(null);
        Key key = Key.of(Id.getPortalMarkerId(portal));
        Marker marker = Marker.icon(BukkitAdapter.point(portal.getExit()), Key.of(Icon.fromPortal(portal).name()), 1);
        marker.markerOptions(MarkerOptions.builder().hoverTooltip(DescriptionBuilder.createDescription(portal)));
        ((SimpleLayerProvider) mapWorld.layerRegistry().get(STARGATE_LAYER)).addMarker(key, marker);
    }

    @Override
    public void deleteMarkerSet() {
        squaremapAPI.mapWorlds().forEach((world) -> {
            world.layerRegistry().unregister(STARGATE_LAYER);
        });
    }

    @Override
    public void registerIcon(InputStream image, Icon key, String type, String title) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(image));
        squaremapAPI.iconRegistry().register(Key.of(key.name()), bufferedImage);
    }
}
