package de.coinshop.gui;

import de.coinshop.CoinShopPlugin;
import de.coinshop.ItemUtil;
import de.coinshop.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Das Hauptmenü, von oben nach unten: Alltag (Shop, Aufgaben, Casino, Investitionen), Bereiche mit Lizenz
 * (Großhandel, Expeditionen, Schwarzmarkt - sie erscheinen erst mit der Lizenz), Ich und Info, zuletzt die Lizenzen.
 * Wer noch nichts besitzt, sieht zuerst das Einsteiger-Menü.
 */
public final class MainMenu extends Menu {

    private static final int[] LICENSED = {20, 22, 24};

    private boolean built;
    private int next;

    public MainMenu(CoinShopPlugin plugin) {
        super(plugin, 5, plugin.messages().get("gui.main.title"));
    }

    private void licensed(Material icon, String name, Action action, String... lines) {
        if (next < LICENSED.length) {
            entry(LICENSED[next++], icon, name, action, lines);
        }
    }

    private void build(Player player) {
        Messages msg = plugin.messages();
        var licenses = plugin.licenses();
        var uuid = player.getUniqueId();

        guide(4, "Von hier erreichst du alles im Shop.",
                "Tagesaufgaben und Shop sind der Einstieg.",
                "Neue Bereiche schaltest du mit Lizenzen frei.");

        entry(10, Material.ENDER_CHEST, "Shop", (p, c) -> new ShopHubMenu(plugin, p).open(p),
                "Server-Shop und Spieler-Shop.");
        entry(12, Material.WRITABLE_BOOK, "Tagesaufgaben", (p, c) -> new QuestMenu(plugin, p).open(p),
                "Jeden Tag neue Aufgaben mit Belohnung.");
        entry(14, Material.NETHER_STAR, "Casino", (p, c) -> new CasinoMenu(plugin, p).open(p),
                "Glücksrad, Automat, Rubbellos.");
        entry(16, Material.GOLD_BLOCK, "Investitionen", (p, c) -> new InvestHubMenu(plugin, p).open(p),
                "Item-Sets für tägliches Einkommen.");

        if (licenses.has(uuid, "grosshandel")) {
            licensed(Material.BARREL, "Großhandel", (p, c) -> plugin.wholesale().openFor(p),
                    "Aufträge, Branchen und deine Firma.");
        }
        if (plugin.expeditions().enabled() && (licenses.has(uuid, "expedition") || licenses.has(uuid, "bergbau"))) {
            licensed(Material.DIAMOND_SWORD, "Expeditionen", (p, c) -> new ExpeditionMenu(plugin, p).open(p),
                    "Abenteuer, Begleiter, Ausrüstung.");
        }
        if (plugin.blackMarket().unlocked(player)) {
            licensed(Material.WITHER_SKELETON_SKULL, "Schwarzmarkt", (p, c) -> new BlackMarketHubMenu(plugin, p).open(p),
                    "Billige Ware, Reviere, Kunden.");
        }

        entry(29, Material.PLAYER_HEAD, "Ich und Fortschritt", (p, c) -> new AreaMenu(plugin, p, AreaMenu.Area.ME).open(p),
                "Profil, Titel, Ranglisten,", "Sammlungen, Erfolge.");
        entry(31, Material.BELL, "Info und Konto", (p, c) -> new AreaMenu(plugin, p, AreaMenu.Area.INFO).open(p),
                "Heute, Einkommen, Buchhaltung,", "Geldumlauf, Zeitplan.");
        entry(33, Material.PAPER, "Lizenzen", (p, c) -> new LicenseMenu(plugin, p).open(p),
                "Dauerhafte Vorteile und neue Bereiche.");

        set(39, balanceIcon(player), incomeClick());
        set(40, ItemUtil.icon(Material.BARRIER, msg.get("gui.close-name"), List.of()), (p, click) -> p.closeInventory());
        set(41, ItemUtil.icon(Material.GOLD_INGOT, msg.get("gui.main.shop-account-name"),
                msg.getList("gui.main.shop-account-lore", "coins", plugin.database().getShopAccount())), null);
        fillEmpty();
    }

    @Override
    public void open(Player player) {
        if (StartMenu.FROM.remove(player.getUniqueId())) {
            new StartMenu(plugin, player).open(player); // zurück zur Shophilfe, wenn man von dort kam
            return;
        }
        if (StartMenu.beginner(plugin, player)) {
            new StartMenu(plugin, player).open(player);
            return;
        }
        if (!built) {
            built = true;
            build(player);
        }
        super.open(player);
    }
}
