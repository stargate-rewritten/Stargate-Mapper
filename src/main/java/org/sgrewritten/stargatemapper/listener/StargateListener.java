package org.sgrewritten.stargatemapper.listener;

import org.sgrewritten.stargatemapper.hook.MapperHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.sgrewritten.stargate.api.event.portal.StargateClosePortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateCreatePortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateDestroyPortalEvent;
import org.sgrewritten.stargate.api.event.portal.StargateOpenPortalEvent;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

import java.util.Collection;

public class StargateListener implements Listener {


    private final Collection<MapperHook> mapperHooks;

    public StargateListener(Collection<MapperHook> mapperHooks) {
        this.mapperHooks = mapperHooks;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onStargateDestroyPortal(StargateDestroyPortalEvent event) {
        Portal destroyedPortal = event.getPortal();
        for (MapperHook mapperHook : this.mapperHooks) {
            if (destroyedPortal instanceof RealPortal) {
                mapperHook.removePortalMarker((RealPortal) destroyedPortal);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onStargateCreatePortal(StargateCreatePortalEvent event) {
        if (event.getDeny()) {
            return;
        }
        Portal portal = event.getPortal();
        for (MapperHook mapperHook : this.mapperHooks) {
            if (portal instanceof RealPortal) {
                mapperHook.addPortalMarker((RealPortal) portal);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onStargateOpenPortal(StargateOpenPortalEvent event) {
        Portal openedPortal = event.getPortal();
        for (MapperHook mapperHook : this.mapperHooks) {
            if (openedPortal instanceof RealPortal) {
                mapperHook.removePortalMarker((RealPortal) openedPortal);
                mapperHook.addPortalMarker((RealPortal) openedPortal);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onStargateClosePortal(StargateClosePortalEvent event) {
        Portal closedPortal = event.getPortal();
        for (MapperHook mapperHook : this.mapperHooks) {
            if (closedPortal instanceof RealPortal) {
                mapperHook.removePortalMarker((RealPortal) closedPortal);
                mapperHook.addPortalMarker((RealPortal) closedPortal);
            }
        }
    }
}
