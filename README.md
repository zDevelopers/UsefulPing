# UsefulPing [![Build Status](https://jenkins.carrade.eu/job/UsefulPing/badge/icon)](https://jenkins.carrade.eu/job/UsefulPing/)

An useful and lightweight `/ping` command with latency and server load, for Bukkit. [Download link](https://jenkins.carrade.eu/job/UsefulPing/).

```
/ping            — displays own ping
/ping <player>   — displays ping for this player

Aliases: /lag, /latency
```

Permission: `ping.ping` (default `true`).

![Preview](https://raw.carrade.eu/s/1463868237.png)

## Diagnostics

If the latency or server load is high, the command displays a diagnostic to help the player understand
its performances problems.

Permission: `ping.diagnostic` (default `true`).

![Diagnostics preview](https://i.zcraft.fr/3523981572177619.png)

## Monitoring

By using `/toggleping` (or clicking on “keep displayed” in the `/ping` output), the ping can be kept
displayed above the inventory, on the action bar. That can be useful to monitor your connection, if
it is bad, or the server performances.

Permission: `ping.toggleping` (default `true`).

![Continuous ping](https://i.zcraft.fr/3928871572177775.png)

## Translations

Translated in French (see configuration file). Use the [provided POT file](src/main/resources/i18n/useful-ping.pot)
if you want to translate it in your language (and send me the translation, if you want it to be available for
others too).