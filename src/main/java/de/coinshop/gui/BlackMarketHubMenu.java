package de.coinshop.gui;

import de.coinshop.CoinShopPlugin;
import de.coinshop.ItemUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;


/**
 * Das Hauptmenü des Schwarzmarkts (nur mit der Schwarzmarktlizenz). Oben die Grundfunktionen, darunter die Bereiche,
 * die erst mit der jeweiligen Lizenz erscheinen (Drogenhandel, Sklavenhandel).
 */
public final class BlackMarketHubMenu extends Menu {

    private final CoinShopPlugin pl;

    @Override
    public void open(Player player) {
        if (!pl.blackMarket().unlocked(player)) {
            pl.messages().send(player, "bm-locked");
            return; // ohne Schwarzmarktlizenz kommt man nicht hinein
        }
        super.open(player);
    }

    public BlackMarketHubMenu(CoinShopPlugin plugin, Player viewer) {
        super(plugin, 4, plugin.messages().get("gui.bmhub.title"));
        this.pl = plugin;

        guide(4, "Hier gibt es billige Ware, aber mit Risiko.",
                "Neue Bereiche schaltest du mit den",
                "Schwarzmarkt-Lizenzen frei (ganz unten).");

        entry(10, Material.WITHER_SKELETON_SKULL, "Händler-Shop", (p, c) -> new BlackMarketMenu(plugin, p).open(p),
                "Täglich 10 wertvolle Items, billig,", "aber mit Fehlkauf-Risiko.");
        entry(12, Material.GOLD_INGOT, "Reviere", (p, c) -> new ReviereMenu(plugin, p).open(p),
                "Stelle Items zum Verkauf an den Händler ein.");
        entry(14, Material.WRITTEN_BOOK, "Kunden", (p, c) -> new BlackMarketMembersMenu(plugin, p).open(p),
                "Alle Spieler mit Schwarzmarkt.");

        // Lizenzen, die neue Bereiche freischalten, stehen zusammen ganz unten
        int slot = 20;
        if (plugin.drugs().enabled() && plugin.licenses().has(viewer.getUniqueId(), "drogenhandel")) {
            entry(slot++, Material.BREWING_STAND, "Drogenhandel", (p, c) -> new DrugHubMenu(plugin, p).open(p),
                    "Labore, Dealer und Kunden in deinen Revieren.");
        }
        if (plugin.creatureAuction().enabled() && plugin.licenses().has(viewer.getUniqueId(), "sklavenhandel")) {
            entry(slot++, Material.LEAD, "Sklavenhandel", (p, c) -> new CreatureAuctionMenu(plugin, p).open(p),
                    "Kreaturen ersteigern und verkaufen.");
        }
        entry(25, Material.ENCHANTED_BOOK, "Schwarzmarkt-Lizenzen", (p, c) -> new LicenseMenu(plugin, p, true).open(p),
                "Vorteile und Bereiche des Schwarzmarkts.");
        set(27, backButton(), (p, c) -> new MainMenu(plugin).open(p));
        set(35, balanceIcon(viewer), incomeClick());
        fillEmpty();
    }
}
