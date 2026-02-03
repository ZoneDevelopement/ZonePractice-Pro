package dev.nandi0813.practice.manager.sidebar;

import dev.nandi0813.practice.manager.sidebar.adapter.SidebarAdapter;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout;
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent;
import org.bukkit.entity.Player;

public class PracticeSidebar {

    private final SidebarManager sidebarManager;
    @Getter
    private final Sidebar sidebar;
    private final Player player;

    public PracticeSidebar(SidebarManager sidebarManager, Sidebar sidebar, Player player) {
        this.sidebarManager = sidebarManager;
        this.sidebar = sidebar;
        this.player = player;

        sidebar.addPlayer(player);
    }

    public void update() {
        // Check if player is still online and sidebar is not closed
        if (!player.isOnline() || sidebar.closed()) return;

        try {
            SidebarAdapter sidebarAdapter = sidebarManager.getSidebarAdapter();
            SidebarComponent.Builder lines = SidebarComponent.builder();

            SidebarComponent title = SidebarComponent.staticLine(sidebarAdapter.getTitle(player));

            for (Component component : sidebarAdapter.getLines(player)) {
                lines.addStaticLine(component);
            }

            ComponentSidebarLayout componentSidebarLayout = new ComponentSidebarLayout(title, lines.build());

            // Double-check before applying in case sidebar was closed during processing
            if (!sidebar.closed()) {
                componentSidebarLayout.apply(sidebar);
            }
        } catch (IllegalStateException e) {
            // Sidebar was closed during update, ignore
        }
    }

}
