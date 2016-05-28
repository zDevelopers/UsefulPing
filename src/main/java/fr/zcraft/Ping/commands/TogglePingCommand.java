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
package fr.zcraft.Ping.commands;

import fr.zcraft.Ping.ContinuousPingSender;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.tools.text.RawMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TogglePingCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(I.t("{ce}You cannot execute this command from the console."));
            return true;
        }

        if (ContinuousPingSender.get().toggleForPlayer(((Player) sender).getUniqueId()))
        {
            sender.sendMessage(I.t("{cs}The ping will be kept in your action bar."));

            RawMessage.send(sender, new RawText(I.t("{cst}Run {cc}/toggleping{cst} or click here to remove."))
                    .hover(new RawText(I.t("Click here to run {cc}/toggleping")))
                    .command("/toggleping")
            );
        }
        else
        {
            sender.sendMessage(I.t("{cs}The ping will no longer be kept."));
        }

        return true;
    }
}
