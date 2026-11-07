package de.coinshop.gui;

import de.coinshop.CoinShopPlugin;
import de.coinshop.ExpeditionManager;
import de.coinshop.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Lager für Ausrüstung, die du gerade nicht brauchst. Sie kommt nicht mit ins Level; ein Klick holt sie zurück. */
public final class ExpeditionStorageMenu extends Menu {

    public ExpeditionStorageMenu(CoinShopPlugin plugin, Player viewer) {
        super(plugin, 6, Component.text("Ausrüstungs-Lager"));
        ExpeditionManager ex = plugin.expeditions();
        var uuid = viewer.getUniqueId();
        List<ExpeditionManager.KitItem> stored = ex.storedItems(uuid);
        for (int i = 0; i < stored.size() && i < 45; i++) {
            ExpeditionManager.KitItem kit = stored.get(i);
            ItemStack shown = kit.item().clone();
            ItemMeta meta = shown.getItemMeta();
            List<Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
            lore.add(ExpeditionMenu.line("Klicken: zurück in den Rucksack", NamedTextColor.GREEN));
            meta.lore(lore);
            shown.setItemMeta(meta);
            set(i, shown, (p, c) -> {
                ex.unstoreKitItem(p.getUniqueId(), kit.key());
                new ExpeditionStorageMenu(plugin, p).open(p);
            });
        }
        if (stored.isEmpty()) {
            set(22, ItemUtil.icon(Material.BARRIER, ExpeditionMenu.line("Das Lager ist leer", NamedTextColor.GRAY), List.of(
                    ExpeditionMenu.line("Wähle beim Sortieren einen Gegenstand", NamedTextColor.GRAY),
                    ExpeditionMenu.line("und klicke dann auf die Truhe.", NamedTextColor.GRAY))), null);
        }
        set(45, backButton(), (p, c) -> new ExpeditionLayoutMenu(plugin, p, -1).open(p));
        fillEmpty();
    }
}
