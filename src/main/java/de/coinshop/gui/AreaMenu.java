package de.coinshop.gui;

import de.coinshop.CoinShopPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** Untermenüs des Hauptmenüs: Ich und Fortschritt, Info und Konto. */
public final class AreaMenu extends Menu {

    /** Die Bereiche des Hauptmenüs. */
    public enum Area {
        ME("Ich und Fortschritt"),
        INFO("Info und Konto");

        final String title;

        Area(String title) {
            this.title = title;
        }
    }

    public AreaMenu(CoinShopPlugin plugin, Player viewer, Area area) {
        super(plugin, 4, Component.text(area.title));
        switch (area) {
            case ME -> {
                guide(4, "Alles über dich: Werte, Titel und Fortschritt.",
                        "Titel und Ranglisten bringen Vorteile.");
                entry(10, Material.PLAYER_HEAD, "Profil", (p, c) -> new ProfileMenu(plugin, p, p.getUniqueId()).open(p),
                        "Deine Werte und Statistiken.");
                entry(12, Material.NAME_TAG, "Titel", (p, c) -> new TitleMenu(plugin, p).open(p),
                        "Wähle deinen Titel. Manche wirken.");
                entry(14, Material.GOLDEN_HELMET, "Ranglisten", (p, c) -> new RankingMenu(plugin, p).open(p),
                        "Wer ist der Beste? Platz 1 bringt Boni.");
                entry(16, Material.BOOKSHELF, "Sammlungen", (p, c) -> new CollectionsMenu(plugin, p).open(p),
                        "Sammle Items, bekomme Belohnungen.");
                entry(22, Material.ENDER_EYE, "Geheime Erfolge", (p, c) -> new SecretMenu(plugin, p).open(p),
                        "Versteckte Erfolge mit Belohnung.");
            }
            case INFO -> {
                guide(4, "Zahlen und Termine rund um dein Geld.",
                        "Dein Kontostand führt zur Einkommens-Übersicht.");
                entry(10, Material.CLOCK, "Heute", (p, c) -> new TodayMenu(plugin, p).open(p),
                        "Was heute offen ist.");
                entry(12, Material.SUNFLOWER, "Einkommen", (p, c) -> new MyIncomeMenu(plugin, p).open(p),
                        "Woher dein Geld kommt.");
                entry(14, Material.WRITABLE_BOOK, "Buchhaltung", (p, c) -> new LedgerMenu(plugin, p, 0, 0).open(p),
                        "Einnahmen und Ausgaben, auch pro Woche.");
                entry(16, Material.GOLD_BLOCK, "Geldumlauf", (p, c) -> new MoneySupplyMenu(plugin, p, true).open(p),
                        "Coins im Umlauf, Verlauf, Reichste.");
                entry(22, Material.COMPASS, "Zeitplan", (p, c) -> new TimelineMenu(plugin, p).open(p),
                        "Start, Nether und Endkampf.");
            }
        }
        set(27, backButton(), (p, c) -> new MainMenu(plugin).open(p));
        set(35, balanceIcon(viewer), incomeClick());
        fillEmpty();
    }
}
