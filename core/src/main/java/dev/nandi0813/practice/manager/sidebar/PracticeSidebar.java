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
        if (sidebar.closed()) return;

        SidebarAdapter sidebarAdapter = sidebarManager.getSidebarAdapter();
        SidebarComponent.Builder lines = SidebarComponent.builder();

        SidebarComponent title = SidebarComponent.staticLine(sidebarAdapter.getTitle(player));

        for (Component component : sidebarAdapter.getLines(player)) {
            lines.addStaticLine(component);
        }

        ComponentSidebarLayout componentSidebarLayout = new ComponentSidebarLayout(title, lines.build());
        componentSidebarLayout.apply(sidebar);
    }

}
