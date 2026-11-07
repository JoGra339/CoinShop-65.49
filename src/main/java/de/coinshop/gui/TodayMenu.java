package de.coinshop.gui;

import de.coinshop.CoinShopPlugin;
import de.coinshop.ExpeditionManager;
import de.coinshop.ItemUtil;
import de.coinshop.QuestManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Heute: ein Überblick über das, was jetzt offen ist (Aufgaben, Abenteuer, Großhandel). */
public final class TodayMenu extends Menu {

    public TodayMenu(CoinShopPlugin plugin, Player viewer) {
        super(plugin, 3, Component.text("Heute"));
        UUID uuid = viewer.getUniqueId();
        ExpeditionManager ex = plugin.expeditions();

        List<Component> quest = new ArrayList<>();
        int done = 0;
        int total = 0;
        try {
            for (QuestManager.Progress pr : plugin.quests().check(viewer)) {
                total++;
                if (pr.done()) {
                    done++;
                }
            }
        } catch (RuntimeException ignored) {
            // Aufgaben gerade nicht verfügbar
        }
        quest.add(ExpeditionMenu.line(total == 0 ? "Heute gibt es keine Aufgaben." : done + " von " + total + " erledigt",
                done == total && total > 0 ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
        quest.add(ExpeditionMenu.line("Klicken: Aufgaben öffnen", NamedTextColor.GREEN));
        set(10, ItemUtil.icon(Material.WRITABLE_BOOK, ExpeditionMenu.line("Tagesaufgaben", NamedTextColor.GOLD), quest),
                (p, c) -> new QuestMenu(plugin, p).open(p));

        List<Component> exp = new ArrayList<>();
        exp.add(ExpeditionMenu.line("Nächstes Level: " + ex.nextLevel(uuid) + " von " + ExpeditionManager.MAX_LEVEL, NamedTextColor.YELLOW));
        exp.add(ExpeditionMenu.line("Klicken: Abenteuer öffnen", NamedTextColor.GREEN));
        set(12, ItemUtil.icon(Material.DIAMOND_SWORD, ExpeditionMenu.line("Abenteuer", NamedTextColor.GOLD), exp),
                (p, c) -> new AdventureMenu(plugin, p).open(p));

        List<Component> wh = new ArrayList<>();
        int working = ex.life().assigned(uuid).size();
        wh.add(ExpeditionMenu.line(working + " Begleiter arbeiten im Großhandel", working > 0 ? NamedTextColor.GREEN : NamedTextColor.GRAY));
        wh.add(ExpeditionMenu.line("Sie liefern beim Shop-Wechsel.", NamedTextColor.GRAY));
        wh.add(ExpeditionMenu.line("Klicken: Großhandel öffnen", NamedTextColor.GREEN));
        set(14, ItemUtil.icon(Material.BARREL, ExpeditionMenu.line("Großhandel", NamedTextColor.GOLD), wh),
                (p, c) -> new WholesaleHubMenu(plugin, p).open(p));

        set(18, backButton(), (p, c) -> new AreaMenu(plugin, p, AreaMenu.Area.INFO).open(p));
        set(22, balanceIcon(viewer), incomeClick());
        fillEmpty();
    }
}
