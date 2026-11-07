package de.coinshop.gui;

import de.coinshop.CoinShopPlugin;
import de.coinshop.ExpeditionManager;
import de.coinshop.ExpeditionManager.Util;
import de.coinshop.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Utensilien: einmalig kaufen, zum Start jedes Levels im Rucksack. */
public final class ExpeditionUtilMenu extends Menu {

    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    public ExpeditionUtilMenu(CoinShopPlugin plugin, Player viewer) {
        super(plugin, 6, Component.text("Expeditions-Utensilien"));
        ExpeditionManager ex = plugin.expeditions();
        var uuid = viewer.getUniqueId();
        Util[] utils = Util.values();
        for (int i = 0; i < utils.length && i < SLOTS.length; i++) {
            Util util = utils[i];
            int level = ex.utilityLevel(uuid, util);
            boolean owned = level > 0;
            int max = ex.utilityMax(util);
            List<Component> lore = new ArrayList<>();
            if (owned) {
                lore.add(ExpeditionMenu.line("Gekauft – du erhältst es zu jedem Levelstart.", NamedTextColor.GREEN));
                if (max > 1) {
                    lore.add(ExpeditionMenu.line("Stufe " + level + " von " + max + ": " + ex.utilityEffect(util, level), NamedTextColor.YELLOW));
                    long up = ex.utilityUpgradePrice(util, level);
                    if (up > 0) {
                        lore.add(ExpeditionMenu.line("Nächste Stufe: " + ex.utilityEffect(util, level + 1), NamedTextColor.AQUA));
                        lore.add(ExpeditionMenu.line("Aufwerten: " + up + " Coins – Klicken", NamedTextColor.GOLD));
                    } else {
                        lore.add(ExpeditionMenu.line("Höchste Stufe erreicht.", NamedTextColor.GREEN));
                    }
                }
            } else {
                lore.add(ExpeditionMenu.line("Stufe 1: " + ex.utilityEffect(util, 1), NamedTextColor.YELLOW));
                if (max > 1) {
                    lore.add(ExpeditionMenu.line("Aufwertbar bis Stufe " + max, NamedTextColor.GRAY));
                }
                lore.add(ExpeditionMenu.line("Preis: " + ex.utilityPrice(util) + " Coins (einmalig)", NamedTextColor.GOLD));
                lore.add(ExpeditionMenu.line("Klicken zum Kaufen", NamedTextColor.GREEN));
            }
            ItemStack shown = ex.utilityStack(util, Math.max(1, level));
            if (shown == null) {
                shown = ItemUtil.icon(util.icon, ExpeditionMenu.line(util.label, NamedTextColor.GOLD), lore);
            } else {
                shown = shown.clone();
                ItemMeta meta = shown.getItemMeta();
                List<Component> all = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
                all.add(ExpeditionMenu.line(util.label, NamedTextColor.GOLD));
                all.addAll(lore);
                meta.lore(all);
                shown.setItemMeta(meta);
            }
            set(SLOTS[i], shown, (p, c) -> {
                if (ex.hasUtility(p.getUniqueId(), util)) {
                    ex.upgradeUtility(p, util);
                } else {
                    ex.buyUtility(p, util);
                }
                new ExpeditionUtilMenu(plugin, p).open(p);
            });
        }
        set(45, backButton(), (p, c) -> new ExpeditionGearMenu(plugin, p).open(p));
        fillEmpty();
    }

    @Override
    public void open(Player player) {
        set(53, balanceIcon(player), incomeClick());
        super.open(player);
    }
}
