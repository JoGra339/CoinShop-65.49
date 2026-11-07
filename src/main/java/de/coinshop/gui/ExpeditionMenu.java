package de.coinshop.gui;

import de.coinshop.CoinShopPlugin;
import de.coinshop.ExpeditionManager;
import de.coinshop.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** Hauptmenü der Expeditionen: Abenteuer (Kämpfen), Begleiter, Ausrüstung und Belohnungen. */
public final class ExpeditionMenu extends Menu {

    static Component line(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }

    private final Player viewer;

    public ExpeditionMenu(CoinShopPlugin plugin, Player viewer) {
        super(plugin, 4, Component.text("Expeditionen"));
        this.viewer = viewer;
        ExpeditionManager ex = plugin.expeditions();
        var uuid = viewer.getUniqueId();

        guide(4, "Abenteuer: Level, Verteidigung, Tiefenmine.",
                "Begleiter kämpfen mit dir und gehen auf Reisen.",
                "Ausrüstung und Multiplikator machen dich stärker.");

        entry(10, Material.DIAMOND_SWORD, "Abenteuer", (p, c) -> new AdventureMenu(plugin, p).open(p),
                "Expeditionslevel, Verteidigung, Tiefenmine.",
                "Nächstes Level: " + ex.nextLevel(uuid) + " von " + ExpeditionManager.MAX_LEVEL);
        entry(12, Material.WOLF_ARMOR, "Begleiter", (p, c) -> new ExpeditionCompanionHubMenu(plugin, p).open(p),
                "Kaufen, mitnehmen, Reisen, Großhandel.",
                "Plätze: " + ex.slotsUnlocked(uuid) + ", dabei: " + ex.companionsTaken(uuid));
        entry(14, Material.NETHERITE_CHESTPLATE, "Ausrüstung", (p, c) -> new ExpeditionGearMenu(plugin, p).open(p),
                "Start-Ausrüstung mit Coins verbessern.");
        entry(16, Material.CHEST, "Inventar sortieren", (p, c) -> new ExpeditionLayoutMenu(plugin, p, -1).open(p),
                "Lege fest, wo jedes Item beim Start liegt.");

        int stage = ex.multStage(uuid);
        List<Component> lore = new ArrayList<>();
        lore.add(line("Belohnung pro Level: Levelzahl x " + trim(ex.multFactor(stage)), NamedTextColor.YELLOW));
        lore.add(line("Stufe " + stage + " von " + ex.maxMultStage(), NamedTextColor.GRAY));
        if (stage < ex.maxMultStage()) {
            lore.add(line("Nächste Stufe (x" + trim(ex.multFactor(stage + 1)) + "): " + ex.multPrice(stage + 1) + " Coins", NamedTextColor.GOLD));
            lore.add(line("Klicken: kaufen", NamedTextColor.GREEN));
        } else {
            lore.add(line("Höchste Stufe erreicht", NamedTextColor.GREEN));
        }
        set(20, ItemUtil.icon(Material.EMERALD, line("Belohnungs-Multiplikator", NamedTextColor.GOLD), lore), (p, c) -> {
            ex.upgradeMult(p);
            new ExpeditionMenu(plugin, p).open(p);
        });

        entry(22, Material.NETHER_STAR, "Bestenliste", (p, c) -> new ExpeditionRankMenu(plugin, p).open(p),
                "Höchstes Level, Prüfungen, Verteidigung.");

        set(24, ItemUtil.icon(Material.BOOK, line("Hilfe im Chat", NamedTextColor.GOLD), List.of(
                line("Kurze Erklärung. Auch mit /expedition hilfe.", NamedTextColor.GRAY),
                line("Klicken: anzeigen", NamedTextColor.GREEN))), (p, c) -> {
            p.closeInventory();
            ex.sendHelp(p);
        });

        set(27, backButton(), (p, c) -> new MainMenu(plugin).open(p));
        fillEmpty();
    }

    public static String trim(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format(java.util.Locale.ROOT, "%.1f", v);
    }

    @Override
    public void open(Player player) {
        set(35, balanceIcon(player), incomeClick());
        super.open(player);
    }
}
