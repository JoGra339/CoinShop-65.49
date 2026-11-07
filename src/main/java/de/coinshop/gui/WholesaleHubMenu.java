package de.coinshop.gui;

import de.coinshop.CoinShopPlugin;
import de.coinshop.ItemUtil;
import de.coinshop.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Das Hauptmenü des Großhandels (nur mit der Großhandellizenz). */
public final class WholesaleHubMenu extends Menu {

    private final CoinShopPlugin pl;

    @Override
    public void open(Player player) {
        if (!pl.wholesale().allowed(player)) {
            return; // ohne Grosshandellizenz kommt man nicht hinein
        }
        super.open(player);
    }

    public WholesaleHubMenu(CoinShopPlugin plugin, Player viewer) {
        super(plugin, 4, plugin.messages().get("gui.whhub.title"));
        this.pl = plugin;
        guide(4, "Jeden Tag gibt es pro Branche einen Großauftrag.",
                "Liefere die Items und kassiere die Prämie.",
                "Mit einer Firma bekommst du Angestellte.");
        entry(10, Material.BARREL, "Branchen und Aufträge", (p, c) -> new WholesaleMenu(plugin, p).open(p),
                "Aufträge erledigen, Branchen freischalten.");
        entry(12, Material.OAK_SIGN, "Mein Gewerbe", (p, c) -> {
            if (plugin.companies().employerOf(p.getUniqueId()) != null) {
                new StaffHomeMenu(plugin, p).open(p);
            } else {
                new CompanyMenu(plugin, p).open(p);
            }
        }, "Deine Firma oder dein Arbeitsplatz.");
        entry(14, Material.WRITABLE_BOOK, "Handelsabkommen", (p, c) -> new TreatyMenu(plugin, p).open(p),
                "Abkommen zwischen Branchen.");
        entry(16, Material.GOLDEN_HELMET, "Ranglisten und Firmen", (p, c) -> new WholesaleRanksMenu(plugin, p).open(p),
                "Rangliste, Firmenrang, alle Firmen.");
        if (plugin.expeditions().enabled()) {
            entry(22, Material.LEAD, "Begleiter zuteilen", (p, c) -> ExpeditionCopyMenu.forWholesale(plugin, p,
                    q -> new WholesaleHubMenu(plugin, q).open(q)).open(p),
                    "Begleiter liefern täglich Items", "an deine Aufträge.");
        }
        set(27, backButton(), (p, c) -> new MainMenu(plugin).open(p));
        set(35, balanceIcon(viewer), incomeClick());
        fillEmpty();
    }
}
