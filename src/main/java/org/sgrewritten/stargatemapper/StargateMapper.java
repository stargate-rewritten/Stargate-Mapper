package org.sgrewritten.stargatemapper;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.network.StorageType;
import org.sgrewritten.stargatemapper.hook.*;
import org.sgrewritten.stargatemapper.listener.StargateListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Feel free to change the package name and project license.
 */
public final class StargateMapper extends JavaPlugin {

    StargateAPI stargateAPI;
    List<MapperHook> mapperHooks;

    @Override
    public void onEnable() {

        //Get the Stargate API
        ServicesManager servicesManager = this.getServer().getServicesManager();
        RegisteredServiceProvider<StargateAPI> stargateProvider = servicesManager.getRegistration(StargateAPI.class);
        if (stargateProvider != null) {
            stargateAPI = stargateProvider.getProvider();
        } else {
            throw new IllegalStateException("Unable to hook into Stargate. Make sure the Stargate plugin is installed " +
                    "and enabled.");
        }
        mapperHooks = getMappers();
        registerIcons();
        this.addAllPortalMarkers();
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(new StargateListener(mapperHooks), this);
    }

    private void registerIcons() {
        for (Icon icon : Icon.values()) {
            try (InputStream inputStream = StargateMapper.class.getResourceAsStream(icon.getFileName())) {
                String type = icon.getFileName().split("\\.")[1].toLowerCase();
                String title = "Stargate " + icon.name().toLowerCase().replaceAll("_", " ");
                byte[] bytes = inputStream.readAllBytes();
                for (MapperHook mapperHook : mapperHooks) {
                    mapperHook.registerIcon(new ByteArrayInputStream(bytes), icon, type, title);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<MapperHook> getMappers() {
        List<MapperHook> mapperHooks = new ArrayList<>();
        PluginManager pluginManager = this.getServer().getPluginManager();
        if (pluginManager.isPluginEnabled("dynmap")) {
            mapperHooks.add(new DynmapHook(pluginManager, this.getLogger()));
        }
        if (pluginManager.isPluginEnabled("pl3xmap")) {
            mapperHooks.add(new Pl3xmapHook(pluginManager));
        }
        if (pluginManager.isPluginEnabled("squaremap")) {
            mapperHooks.add(new SquaremapHook(pluginManager));
        }
        if (pluginManager.isPluginEnabled("BlueMap")) {
            mapperHooks.add(new BluemapHook(this.getLogger(), this));
        }
        if (mapperHooks.isEmpty()) {
            throw new IllegalStateException("No supported map interface found, expected any of [dynmap, pl3xmap, squaremap, bluemap].");
        }
        return mapperHooks;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (MapperHook mapperHook : this.mapperHooks) {
            mapperHook.deleteMarkerSet();
        }
    }

    /**
     * Gets all portals registered to Stargate
     *
     * @return <p>All portals registered to Stargate</p>
     */
    public List<Portal> getAllPortals() {
        List<Portal> portals = new ArrayList<>();
        Stream<Network> localNetworkStream = stargateAPI.getRegistry().getNetworkRegistry(StorageType.LOCAL).stream();
        Stream<Network> interserverNetworkStream = stargateAPI.getRegistry().getNetworkRegistry(StorageType.INTER_SERVER).stream();
        Stream.concat(localNetworkStream, interserverNetworkStream)
                .map(Network::getAllPortals)
                .forEach(portals::addAll);
        return portals;
    }

    /**
     * Adds portal markers for all registered portals
     */
    private void addAllPortalMarkers() {
        List<Portal> portals = getAllPortals();
        for (MapperHook mapperHook : this.mapperHooks) {
            mapperHook.addPortalMarkers(portals);
        }
    }

}
