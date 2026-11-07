package de.coinshop.gui;

import de.coinshop.CoinShopPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Der Shop: Server-Shop oder Spieler-Shop. */
public final class ShopHubMenu extends Menu {

    public ShopHubMenu(CoinShopPlugin plugin, Player viewer) {
        super(plugin, 3, plugin.messages().get("gui.shophub.title"));
        guide(4, "Server-Shop: feste Preise, täglich neu.",
                "Spieler-Shop: Spieler handeln untereinander.");
        entry(11, Material.ENDER_CHEST, "Server-Shop", (p, c) -> new ServerShopMenu(plugin, p).open(p),
                "14 Angebote, täglich um " + plugin.serverShop().resetHour() + ":00 Uhr neu.");
        entry(15, Material.EMERALD, "Spieler-Shop", (p, c) -> new PlayerShopMenu(plugin, p, 0).open(p),
                "Kaufe von Spielern oder biete selbst an.");
        set(18, backButton(), (p, c) -> new MainMenu(plugin).open(p));
        set(26, balanceIcon(viewer), incomeClick());
        fillEmpty();
    }
}
