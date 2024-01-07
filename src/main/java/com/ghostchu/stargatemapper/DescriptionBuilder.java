package com.ghostchu.stargatemapper;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.sgrewritten.stargate.api.network.portal.RealPortal;

public class DescriptionBuilder {

    public static String createDescription(RealPortal portal){

        String destinationName = portal.getDestinationName();
        if (StringUtils.isEmpty(destinationName)) {
            destinationName = "<Non-directional Stargate>";
        }
        String owner = Bukkit.getOfflinePlayer(portal.getOwnerUUID()).getName();
        if (owner == null) {
            owner = portal.getOwnerUUID().toString();
        }
        return "<b>Name:</b> " + portal.getName() + "<br />" +
                "<b>Network:</b> " + portal.getNetwork().getName() + "<br />" +
                "<b>Destination:</b> " + destinationName + "<br />" +
                "<b>Owner:</b> " + owner + "<br />";
    }
}
