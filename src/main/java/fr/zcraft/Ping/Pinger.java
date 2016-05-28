/*
 * Copyright or © or Copr. AmauryCarrade (2015)
 * 
 * http://amaury.carrade.eu
 * 
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.zcraft.Ping;

import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.reflection.NMSNetwork;
import fr.zcraft.zlib.tools.reflection.Reflection;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;


public final class Pinger
{
    private static Class<?> MinecraftServer = null;
    private static Class<?> CraftServer = null;

    static
    {
        try
        {
            MinecraftServer = Reflection.getMinecraftClassByName("MinecraftServer");
            CraftServer = Reflection.getBukkitClassByName("CraftServer");
        }
        catch (ClassNotFoundException e)
        {
            PluginLogger.error("Unable to load classes required to retrieve TPS; disabling TPS support.", e);
        }
    }


    /**
     * Returns a player's latency
     *
     * @param player The player.
     *
     * @return The latency in milli-seconds, or -1 if an error occurred while
     * retrieving it.
     */
    public static int getPlayerLatency(final Player player)
    {
        if (player == null) return -1;

        try
        {
            return (int) Reflection.getFieldValue(NMSNetwork.getPlayerHandle(player), "ping");
        }
        catch (InvocationTargetException | IllegalAccessException | NoSuchFieldException e)
        {
            PluginLogger.error("Unable to retrieve {0}'s latency.", e, player.getName());
            return -1;
        }
    }

    /**
     * Retrieves the server's TPS.
     *
     * @return An array of three {@link Double doubles} representing the ticks
     * per seconds of the last minute, 5 minutes and 15 minutes.
     */
    public static double[] getServerTPS()
    {
        if (CraftServer == null || MinecraftServer == null) return null;

        try
        {
            Object nmsServer = Reflection.call(CraftServer, Bukkit.getServer(), "getServer");
            return (double[]) Reflection.getFieldValue(MinecraftServer, nmsServer, "recentTps");
        }
        catch (InvocationTargetException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException e)
        {
            PluginLogger.error("Unable to retrieve the last ticks per second.", e);
            return null;
        }
    }

    public static String formatLatency(int latency)
    {
        StringBuilder builder = new StringBuilder();

        if (latency > 350)
            builder.append(ChatColor.RED);
        else if (latency > 150)
            builder.append(ChatColor.YELLOW);
        else
            builder.append(ChatColor.GREEN);

        return builder.append(latency).append(" ms").toString();
    }

    public static String formatTPS(double[] tps)
    {
        String[] formattedTPS = new String[tps.length];

        for (int i = 0, tpsLength = tps.length; i < tpsLength; i++)
        {
            final double tps_value = tps[i];
            final ChatColor color = tps_value > 18.0 ? ChatColor.GREEN : tps_value > 16.0 ? ChatColor.YELLOW : ChatColor.RED;

            formattedTPS[i] = color + (tps_value >= 20.0 ? "*" : "") + Math.min(Math.round(tps_value * 100.0) / 100.0, 20.0);
        }

        return StringUtils.join(formattedTPS, ChatColor.GRAY + ", " + ChatColor.RESET);
    }
}
