package fr.zcraft.Ping;

import fr.zcraft.Ping.commands.PingCommand;
import fr.zcraft.Ping.commands.TogglePingCommand;
import fr.zcraft.quartzlib.components.i18n.I18n;
import fr.zcraft.quartzlib.core.QuartzPlugin;
import fr.zcraft.quartzlib.tools.PluginLogger;
import fr.zcraft.sentrybukkit.SentryBukkit;
import io.sentry.Sentry;


public final class Ping extends QuartzPlugin
{
    @Override
    public void onLoad() {
        SentryBukkit.init(this, "https://f7eafb564dc649018072773d1d2adf4f@o475316.ingest.sentry.io/5727716");
        super.onLoad();
    }

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        loadComponents(I18n.class, PingConfig.class, ContinuousPingSender.class);

        I18n.setPrimaryLocale(PingConfig.LOCALE.get());

        getCommand("ping").setExecutor(new PingCommand());
        getCommand("toggleping").setExecutor(new TogglePingCommand());

        try {
            throw new Exception("This is a toast. Yummy!");
        } catch (Exception e) {
            PluginLogger.error("Sentry will capture", e);
            Sentry.captureException(e);
        }
    }
}
