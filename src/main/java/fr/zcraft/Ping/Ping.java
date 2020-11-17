package fr.zcraft.Ping;

import fr.zcraft.Ping.commands.PingCommand;
import fr.zcraft.Ping.commands.TogglePingCommand;
import fr.zcraft.quartzlib.components.i18n.I18n;
import fr.zcraft.quartzlib.core.QuartzPlugin;


public final class Ping extends QuartzPlugin
{
    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        loadComponents(I18n.class, PingConfig.class, ContinuousPingSender.class);

        I18n.setPrimaryLocale(PingConfig.LOCALE.get());

        getCommand("ping").setExecutor(new PingCommand());
        getCommand("toggleping").setExecutor(new TogglePingCommand());
    }
}
