package de.coinshop.gui;

import de.coinshop.CoinShopPlugin;
import de.coinshop.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Grundbaustein für alle Menüs.
 *
 * Ein Menü ist eine Kiste mit Items. Zu jedem Feld kann eine "Aktion" gehören, die beim
 * Anklicken ausgeführt wird. Das Menü merkt sich selbst als "Besitzer" (InventoryHolder) der
 * Kiste, so erkennt der MenuListener später, dass ein Klick zu einem unserer Menüs gehört.
 */
public abstract class Menu implements InventoryHolder {

    /** Was passiert, wenn ein Feld angeklickt wird. */
    @FunctionalInterface
    public interface Action {
        void run(Player player, ClickType click);
    }

    protected final CoinShopPlugin plugin;
    private final Inventory inventory;
    private final Map<Integer, Action> actions = new HashMap<>();

    protected Menu(CoinShopPlugin plugin, int rows, Component title) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
    }

    /** Legt ein Item in ein Feld; "action" darf null sein (dann ist es nur Deko/Anzeige). */
    protected void set(int slot, ItemStack item, Action action) {
        inventory.setItem(slot, item);
        if (action != null) {
            actions.put(slot, action);
        }
    }

    /** Füllt alle noch leeren Felder mit grauen Glasscheiben. */
    protected void fillEmpty() {
        ItemStack filler = ItemUtil.filler();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack current = inventory.getItem(slot);
            if (current == null || current.getType() == Material.AIR) {
                inventory.setItem(slot, filler);
            }
        }
    }

    /** Die kleine Anleitung eines Bereichs: ein Buch mit wenigen, kurzen Zeilen. */
    protected void guide(int slot, String... lines) {
        List<Component> lore = new ArrayList<>();
        for (String text : lines) {
            lore.add(ExpeditionMenu.line(text, NamedTextColor.GRAY));
        }
        set(slot, ItemUtil.icon(Material.KNOWLEDGE_BOOK, ExpeditionMenu.line("Anleitung", NamedTextColor.AQUA), lore), null);
    }

    /** Ein Knopf mit Name (gold), Kurzbeschreibung (grau) und Klick-Hinweis (grün). */
    protected void entry(int slot, Material icon, String name, Action action, String... lines) {
        List<Component> lore = new ArrayList<>();
        for (String text : lines) {
            lore.add(ExpeditionMenu.line(text, NamedTextColor.GRAY));
        }
        if (action != null) {
            lore.add(ExpeditionMenu.line("Klicken: öffnen", NamedTextColor.GREEN));
        }
        set(slot, ItemUtil.icon(icon, ExpeditionMenu.line(name, NamedTextColor.GOLD), lore), action);
    }

    /** Standard-Knöpfe, die in mehreren Menüs vorkommen. */
    protected ItemStack backButton() {
        return ItemUtil.icon(Material.OAK_DOOR, plugin.messages().get("gui.back-name"), List.of());
    }

    /** Klick auf den Kontostand: öffnet deine Einkommens-Aufstellung. */
    protected Action incomeClick() {
        return (player, click) -> new MyIncomeMenu(plugin, player).open(player);
    }

    protected ItemStack balanceIcon(Player player) {
        long coins = plugin.database().getCoins(player.getUniqueId());
        // Die Sonnenblume ist das Symbol für Coins
        return ItemUtil.icon(Material.SUNFLOWER, plugin.messages().get("gui.balance-name"),
                plugin.messages().getList("gui.balance-lore", "coins", coins));
    }

    /** Wird vom MenuListener aufgerufen, wenn ein Feld dieses Menüs angeklickt wurde. */
    public void handleClick(Player player, int slot, ClickType click) {
        Action action = actions.get(slot);
        if (action != null) {
            action.run(player, click);
        }
    }

    /** Nimmt das Menü Items an, die man hineinzieht oder per Shift-Klick hineinschiebt? */
    public boolean acceptsDrops() {
        return false;
    }

    /** Ein Item wurde hineingelegt. true = angenommen (Aufrufer entfernt es aus dem Inventar). */
    public boolean handleDrop(Player player, ItemStack item) {
        return false;
    }

    /** Nach dem Ablegen eines Items: Menü neu aufbauen. */
    public void refresh(Player player) {
    }

    /** Menüs der Börse (Aktien, Krypto, Miner) überschreiben das mit true: sie brauchen die Börsenlizenz. */
    protected boolean requiresExchange() {
        return false;
    }

    public void open(Player player) {
        if (requiresExchange() && !plugin.licenses().exchangeUnlocked(player.getUniqueId())) {
            plugin.messages().send(player, "exchange-locked");
            return;
        }
        try {
            String key = "menu_open_" + getClass().getSimpleName();
            plugin.database().setMetaLong(key, plugin.database().getMetaLong(key, 0) + 1);
        } catch (RuntimeException ignored) {
            // der Zähler ist nur eine Hilfe für die Admins
        }
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
