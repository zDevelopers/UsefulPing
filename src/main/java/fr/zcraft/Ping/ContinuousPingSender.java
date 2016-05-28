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

import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.core.ZLibComponent;
import fr.zcraft.zlib.tools.runners.RunTask;
import fr.zcraft.zlib.tools.text.MessageSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class ContinuousPingSender extends ZLibComponent implements Listener
{
    private static ContinuousPingSender instance;

    private BukkitTask task = null;
    private Set<UUID> trackedPlayers = new HashSet<>();

    public ContinuousPingSender()
    {
        instance = this;
    }

    public static ContinuousPingSender get() { return instance; }

    public void addPlayer(UUID player)
    {
        trackedPlayers.add(player);
        sendToPlayer(player);
        runTaskIfNeeded();
    }

    public void removePlayer(UUID player)
    {
        trackedPlayers.remove(player);
        runTaskIfNeeded();
    }

    public boolean toggleForPlayer(UUID player)
    {
        if (trackedPlayers.contains(player))
        {
            removePlayer(player);
            return false;
        }
        else
        {
            addPlayer(player);
            return true;
        }
    }

    private void sendToPlayer(UUID id)
    {
        final double[] tps = Pinger.getServerTPS();
        sendToPlayer(id, tps != null ? Pinger.formatTPS(tps) : I.t("{gray}(unknown)"));
    }

    private void sendToPlayer(UUID uuid, String tps)
    {
        final Player player = Bukkit.getPlayer(uuid);

        final int latency = Pinger.getPlayerLatency(player);

        MessageSender.sendActionBarMessage(player, new RawText("")
                        .then(I.t("Latency: ")).color(ChatColor.GOLD)
                        .then(latency != -1 ? Pinger.formatLatency(latency) : I.t("{gray}(unknown)"))

                        .then(" - ").color(ChatColor.GRAY)

                        .then(I.t("Load: ")).color(ChatColor.GOLD)
                        .then(tps)

                        .build()
        );
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerLogout(PlayerQuitEvent ev)
    {
        removePlayer(ev.getPlayer().getUniqueId());
    }

    private void runTaskIfNeeded()
    {
        if (task == null && !trackedPlayers.isEmpty())
        {
            task = RunTask.timer(new Runnable() {
                @Override
                public void run()
                {
                    final double[] tps = Pinger.getServerTPS();
                    final String tpsDisplayed = tps != null ? Pinger.formatTPS(tps) : I.t("{gray}(unknown)");

                    for (UUID player : trackedPlayers)
                        sendToPlayer(player, tpsDisplayed);
                }
            }, 30l, 30l);
        }
        else if (task != null && trackedPlayers.isEmpty())
        {
            task.cancel();
            task = null;
        }
    }
}
