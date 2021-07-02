package com.bilicraft.stargatedynmap;

import net.TheDgtl.Stargate.Portal;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.event.StargateCreateEvent;
import net.TheDgtl.Stargate.event.StargateDestroyEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.lang.reflect.Field;
import java.util.List;

public final class StargateDynmap extends JavaPlugin {
    DynmapAPI dynmapAPI;
    Stargate stargate;
    MarkerSet markerSet;
    MarkerIcon portalIcon;

    @Override
    public void onEnable() {
        // Plugin startup logic
        dynmapAPI = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
        stargate = (Stargate) Bukkit.getPluginManager().getPlugin("Stargate");
        markerSet = dynmapAPI.getMarkerAPI().createMarkerSet("stargate","Stargate",null,false);
        portalIcon = dynmapAPI.getMarkerAPI().getMarkerIcon("portal");
        updatePortals();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        markerSet.deleteMarkerSet();
    }

    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void portalEvent(StargateCreateEvent event){
        if(!event.getDeny()){
            updatePortals();
        }
    }

    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)
    public void portalEvent(StargateDestroyEvent event){
        if(!event.getDeny()){
            updatePortals();
        }
    }

    public List<Portal> getAllPortals(){
        // Stargate API is really terrible
        Field field = null;
        try {
            field = Portal.class.getDeclaredField("allPortals");
            field.setAccessible(true);
            //noinspection unchecked
            return (List<Portal>)field.get(null);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updatePortals(){
        List<Portal> portals = getAllPortals();
        if(portals == null){
            getLogger().warning("Cannot to update portals markers, failed to calling Stargate API.");
            return;
        }
        // Delete all markers
        markerSet.getMarkers().forEach(GenericMarker::deleteMarker);
        portals.forEach(portal->{
            try {
                Field hiddenField = portal.getClass().getDeclaredField("hidden");
                boolean hidden = (boolean) hiddenField.get(portal);
                if(hidden){
                    return;
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }

            Location location = portal.getEntrances()[0].getLocation();
            String destinationName = portal.getDestinationName();
            if(StringUtils.isEmpty(destinationName)){
                destinationName = "<非定向星门>";
            }
            Marker marker = markerSet.createMarker(null,portal.getName(),portal.getWorld().getName(),location.getX(),location.getY(),location.getZ(), portalIcon,false);
            String desc = "名称: " + portal.getName() + "<br />" +
                    "网络: " + portal.getNetwork() + "<br />" +
                    "目的地: " + destinationName + "<br />" +
                    "所有者: " + portal.getOwnerName() + "<br />";
            marker.setDescription(desc);
            marker.setLabel(portal.getName(),true);
            marker.setMarkerIcon(portalIcon);
        });
    }
}
