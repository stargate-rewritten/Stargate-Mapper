package com.ghostchu.stargatedynmap;

import com.ghostchu.stargatedynmap.hook.DynmapHook;
import com.ghostchu.stargatedynmap.hook.MapperHook;
import com.ghostchu.stargatedynmap.hook.Pl3xmapHook;
import com.ghostchu.stargatedynmap.hook.SquaremapHook;
import org.bukkit.plugin.PluginManager;
import org.sgrewritten.stargate.api.StargateAPI;
import org.sgrewritten.stargate.api.network.Network;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Feel free to change the package name and project license.
 */
public final class StargateMapper extends JavaPlugin implements Listener {

    StargateAPI stargateAPI;
    List<MapperHook> mapperHooks;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);

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
        addAllPortalMarkers();
    }

    private void registerIcons() {
        for(Icon icon : Icon.values()){
            try(InputStream inputStream = StargateMapper.class.getResourceAsStream(icon.getFileName())) {
                BufferedImage image = ImageIO.read(Objects.requireNonNull(inputStream));
                String type = icon.getFileName().split("\\.")[1].toLowerCase();
                String title = "Stargate " + icon.name().toLowerCase().replaceAll("_"," ");
                mapperHooks.forEach((mapperHook -> mapperHook.registerIcon(image,icon.name(),type,title)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<MapperHook> getMappers() {
        List<MapperHook> mapperHooks = new ArrayList<>();
        PluginManager pluginManager = this.getServer().getPluginManager();
        if(pluginManager.isPluginEnabled("dynmap")){
            mapperHooks.add(new DynmapHook(pluginManager, this.getLogger()));
        }
        if(pluginManager.isPluginEnabled("pl3xmap")){
            mapperHooks.add(new Pl3xmapHook(pluginManager));
        }
        if(pluginManager.isPluginEnabled("squaremap")){
            mapperHooks.add(new SquaremapHook(pluginManager));
        }
        if(mapperHooks.isEmpty()){
            throw new IllegalStateException("No supported map interface found, expected any of [dynmap, pl3xmap, squaremap].");
        }
        return mapperHooks;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(MapperHook mapperHook : this.mapperHooks) {
            mapperHook.deleteMarkerSet();
        }
    }

    /**
     * Gets all portals registered to Stargate
     *
     * @return <p>All portals registered to Stargate</p>
     */
    private List<Portal> getAllPortals() {
        List<Portal> portals = new ArrayList<>();
        Map<String, Network> networks = stargateAPI.getRegistry().getNetworkMap();
        for (Network network : networks.values()) {
            portals.addAll(network.getAllPortals());
        }
        return portals;
    }

    /**
     * Adds portal markers for all registered portals
     */
    private void addAllPortalMarkers() {
        List<Portal> portals = getAllPortals();
        for(MapperHook mapperHook : this.mapperHooks){
            mapperHook.addPortalMarkers(portals);
        }
    }


}
