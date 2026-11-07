package de.coinshop.gui;

import de.coinshop.CoinShopPlugin;
import de.coinshop.ItemUtil;
import de.coinshop.LootRewards;
import de.coinshop.MysteryBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Öffnen einer Mystery Box mit Animation (wie beim Glücksrad): In der mittleren Reihe laufen die Felder von rechts
 * nach links durch und werden langsamer; das Feld unter dem Pfeil (Mitte) ist der echte Gewinn. Der Inhalt ist
 * schon vor der Animation gewürfelt und gutgeschrieben, die Anzeige und die Chat-Nachricht kommen erst am Ende.
 */
public final class BoxOpenMenu extends Menu {

    private static final Material[] FILLER = {Material.GOLD_INGOT, Material.GOLD_NUGGET, Material.AMETHYST_SHARD, Material.PAPER,
            Material.IRON_PICKAXE, Material.BONE, Material.REDSTONE, Material.EMERALD, Material.DIAMOND, Material.NAME_TAG,
            Material.BARREL, Material.EXPERIENCE_BOTTLE};
    private static final Random RANDOM = new Random();

    private final MysteryBox boxes;
    private final int tier;
    private final LootRewards.Result result;
    private boolean finished;

    private BoxOpenMenu(CoinShopPlugin plugin, Player viewer, int tier, LootRewards.Result result) {
        super(plugin, 3, Component.text("Mystery Box öffnen"));
        this.boxes = plugin.mysteryBoxes();
        this.tier = tier;
        this.result = result;
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(9 + i, filler());
        }
        set(4, ItemUtil.icon(Material.HOPPER, line("Dein Gewinn", NamedTextColor.GOLD), List.of()), null);
        set(22, ItemUtil.icon(Material.ENDER_CHEST, line("Die Box öffnet sich ...", MysteryBox.colorOf(tier)), List.of()), null);
        set(26, balanceIcon(viewer), incomeClick());
        fillEmpty();
    }

    /** Öffnet eine Box der Stufe mit Animation. Bei einem Fehler bleibt der Spieler im Box-Lager. */
    public static void start(CoinShopPlugin plugin, Player player, int tier) {
        LootRewards.Result r = plugin.mysteryBoxes().beginOpen(player, tier);
        if (r == null) {
            new MysteryBoxMenu(plugin, player).open(player);
            return;
        }
        BoxOpenMenu menu = new BoxOpenMenu(plugin, player, tier, r);
        menu.open(player);
        // ein Tick Pause, damit das Menü sicher offen ist, bevor das erste Bild kommt
        int last = 36 + RANDOM.nextInt(12);
        List<ItemStack> strip = new ArrayList<>();
        for (int i = 0; i < last + 9; i++) {
            strip.add(menu.filler());
        }
        strip.set(last + 4, menu.landing());
        Bukkit.getScheduler().runTaskLater(plugin, () -> menu.frame(player, strip, 0, last), 2L);
    }

    private ItemStack filler() {
        Material m = FILLER[RANDOM.nextInt(FILLER.length)];
        return ItemUtil.icon(m, line("Mystery Box", NamedTextColor.DARK_GRAY), List.of());
    }

    /** Das echte Gewinnfeld. */
    private ItemStack landing() {
        ItemStack item = ItemUtil.icon(iconFor(result.text()), line(capital(result.text()), MysteryBox.colorOf(tier)).decorate(TextDecoration.BOLD),
                List.of(line("Stufe " + tier + " von 5", NamedTextColor.GRAY)));
        item.editMeta(m -> m.setEnchantmentGlintOverride(true));
        return item;
    }

    private static String capital(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static Material iconFor(String text) {
        String t = text.toLowerCase();
        if (t.contains("aktien")) {
            return Material.GOLD_NUGGET;
        }
        if (t.contains("krypto")) {
            return Material.AMETHYST_SHARD;
        }
        if (t.contains("lizenz")) {
            return Material.PAPER;
        }
        if (t.contains("miner")) {
            return Material.IRON_PICKAXE;
        }
        if (t.contains("revier") || t.contains("verkaufs-slot")) {
            return Material.BARREL;
        }
        if (t.contains("begleiter") || t.contains("gefolge")) {
            return Material.BONE;
        }
        if (t.contains("ausdauer")) {
            return Material.COOKED_BEEF;
        }
        if (t.contains("ki-")) {
            return Material.REDSTONE;
        }
        if (t.contains("coins")) {
            return Material.GOLD_INGOT;
        }
        return Material.NETHER_STAR;
    }

    /** Ein Bild der Animation; die Pausen werden immer länger (Auslaufen), Ton und Bild kommen im selben Tick. */
    private void frame(Player player, List<ItemStack> strip, int f, int last) {
        if (finished) {
            return;
        }
        // Menü geschlossen oder Spieler weg: sofort fertig (der Gewinn ist ja schon gutgeschrieben)
        if (!player.isOnline() || player.getOpenInventory().getTopInventory().getHolder() != this) {
            finish(player);
            return;
        }
        for (int i = 0; i < 9; i++) {
            getInventory().setItem(9 + i, strip.get(f + i));
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 1.0f + 0.6f * f / Math.max(1, last));
        if (f >= last) {
            finish(player);
            return;
        }
        int remaining = last - f;
        int delay = remaining > 20 ? 1 : remaining > 10 ? 2 : remaining > 4 ? 4 : 7;
        Bukkit.getScheduler().runTaskLater(plugin, () -> frame(player, strip, f + 1, last), delay);
    }

    private void finish(Player player) {
        if (finished) {
            return;
        }
        finished = true;
        // Das Gewinnfeld steht immer in der Mitte, auch wenn die Animation abgebrochen wurde
        getInventory().setItem(13, landing());
        boxes.finishOpen(player, tier, result);
        if (!player.isOnline()) {
            return;
        }
        Sound sound = tier >= 4 ? Sound.UI_TOAST_CHALLENGE_COMPLETE : Sound.ENTITY_PLAYER_LEVELUP;
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        if (player.getOpenInventory().getTopInventory().getHolder() != this) {
            return;
        }
        getInventory().setItem(26, balanceIcon(player));
        int left = boxes.count(player.getUniqueId(), tier);
        set(18, backButton(), (p, c) -> new MysteryBoxMenu(plugin, p).open(p));
        if (left > 0) {
            set(22, ItemUtil.icon(Material.ENDER_CHEST, line("Nächste Box öffnen", NamedTextColor.GREEN),
                    List.of(line("Noch " + left + " Boxen der Stufe " + tier, NamedTextColor.GRAY))),
                    (p, c) -> start(plugin, p, tier));
        } else {
            set(22, ItemUtil.icon(Material.BARRIER, line("Keine Box dieser Stufe mehr", NamedTextColor.DARK_GRAY), List.of()), null);
        }
    }

    private static Component line(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }
}
