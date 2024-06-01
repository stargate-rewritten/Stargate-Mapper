package org.sgrewritten.stargatemapper;

import org.sgrewritten.stargate.api.network.portal.RealPortal;

public enum Icon {

    NETHER_PORTAL_OPEN("/icons/nether_portal_on.png"),
    NETHER_PORTAL_CLOSED("/icons/nether_portal_off.png"),
    UNDERWATER_PORTAL_OPEN("/icons/underwater_on.png"),
    UNDERWATER_PORTAL_CLOSED("/icons/underwater_off.png");



    private final String fileName;

    Icon(String fileName){
        this.fileName = fileName;
    }

    public String getFileName(){
        return this.fileName;
    }

    public static Icon fromPortal(RealPortal portal){
        if(portal.isOpen()){
            return portal.getGate().getFormat().getIrisMaterial(false).isAir() ? Icon.NETHER_PORTAL_OPEN : Icon.UNDERWATER_PORTAL_OPEN;
        } else {
            return portal.getGate().getFormat().getIrisMaterial(false).isAir() ? Icon.NETHER_PORTAL_CLOSED : Icon.UNDERWATER_PORTAL_CLOSED;
        }
    }
}
