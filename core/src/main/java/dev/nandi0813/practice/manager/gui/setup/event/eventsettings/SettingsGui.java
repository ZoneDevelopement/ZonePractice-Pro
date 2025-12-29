package dev.nandi0813.practice.manager.gui.setup.event.eventsettings;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.GUIFile;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.event.EventSetupManager;
import dev.nandi0813.practice.manager.gui.setup.event.EventSetupUtil;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class SettingsGui extends GUI {

    private final EventData eventData;

    public SettingsGui(EventData eventData) {
        super(GUIType.Event_Settings);
        this.gui.put(1, InventoryUtil.createInventory(GUIFile.getString("GUIS.SETUP.EVENT.EVENT-SETTINGS.TITLE").replace("%eventName%", eventData.getType().getName()), 3));
        this.eventData = eventData;

        build();
    }

    @Override
    public void build() {
        // Frame
        for (int i = 0; i < gui.get(1).getSize(); i++)
            gui.get(1).setItem(i, GUIManager.getFILLER_ITEM());

        // Back to Ladder Gui Icon
        gui.get(1).setItem(18, GUIFile.getGuiItem("GUIS.SETUP.EVENT.EVENT-SETTINGS.ICONS.BACK-TO").get());

        update();
    }

    @Override
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(ZonePractice.getInstance(), () ->
        {
            gui.get(1).setItem(10, EventSetupUtil.getBroadcastIntervalItem(eventData.getBroadcastInterval()));
            gui.get(1).setItem(11, EventSetupUtil.getWaitBeforeStartItem(eventData.getWaitBeforeStart()));
            gui.get(1).setItem(12, EventSetupUtil.getMaxQueueTimeItem(eventData.getMaxQueueTime()));
            gui.get(1).setItem(13, EventSetupUtil.getDurationItem(eventData));
            gui.get(1).setItem(14, EventSetupUtil.getStartTimeItem(eventData.getStartTime()));
            gui.get(1).setItem(15, EventSetupUtil.getMinPlayerItem(eventData.getMinPlayer()));
            gui.get(1).setItem(16, EventSetupUtil.getMaxPlayerItem(eventData.getMaxPlayer()));

            updatePlayers();
        });
    }

    @Override
    public void handleClickEvent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getView().getTopInventory();

        ItemStack item = e.getCurrentItem();
        ClickType click = e.getClick();
        int slot = e.getRawSlot();

        e.setCancelled(true);

        if (inventory.getSize() <= slot) return;
        if (item == null) return;
        if (item.equals(GUIManager.getFILLER_ITEM())) return;

        if (slot == 18) {
            EventSetupManager.getInstance().getEventSetupGUIs().get(eventData).get(GUIType.Event_Main).open(player);
            return;
        }

        if (eventData.isEnabled()) {
            Common.sendMMMessage(player, LanguageManager.getString("COMMAND.SETUP.EVENT.CANT-EDIT-ENABLED"));
            return;
        }

        switch (slot) {
            case 10:
                int broadcastInterval = eventData.getBroadcastInterval();

                if (click.isLeftClick() && broadcastInterval > 5)
                    eventData.setBroadcastInterval(broadcastInterval - 1);
                else if (click.isRightClick() && broadcastInterval < 30)
                    eventData.setBroadcastInterval(broadcastInterval + 1);
                break;
            case 11:
                int waitBeforeStart = eventData.getWaitBeforeStart();

                if (click.isLeftClick() && waitBeforeStart > 5)
                    eventData.setWaitBeforeStart(waitBeforeStart - 1);
                else if (click.isRightClick() && waitBeforeStart < 60)
                    eventData.setWaitBeforeStart(waitBeforeStart + 1);
                break;
            case 12:
                int maxQueueTime = eventData.getMaxQueueTime();

                if (click.isLeftClick() && maxQueueTime > 5 * 30)
                    eventData.setMaxQueueTime(maxQueueTime - 60);
                else if (click.isRightClick() && maxQueueTime < 30 * 30)
                    eventData.setMaxQueueTime(maxQueueTime + 60);
                break;
            case 13:
                switch (eventData.getType()) {
                    case TNTTAG:
                        int tntExplode = eventData.getDuration();

                        if (click.isLeftClick() && tntExplode > 10)
                            eventData.setDuration(tntExplode - 1);
                        else if (click.isRightClick() && tntExplode < 60)
                            eventData.setDuration(tntExplode + 1);
                        break;
                    case BRACKETS:
                    case SUMO:
                        int duration = eventData.getDuration();

                        if (click.isLeftClick() && duration > 2 * 60)
                            eventData.setDuration(duration - 60);
                        else if (click.isRightClick() && duration < 15 * 60)
                            eventData.setDuration(duration + 60);
                        break;
                    default:
                        int duration2 = eventData.getDuration();

                        if (click.isLeftClick() && duration2 > 10 * 60)
                            eventData.setDuration(duration2 - 60);
                        else if (click.isRightClick() && duration2 < 75 * 60)
                            eventData.setDuration(duration2 + 60);
                        break;
                }
                break;
            case 14:
                int startTime = eventData.getStartTime();

                if (click.isLeftClick() && startTime > 0)
                    eventData.setStartTime(startTime - 1);
                else if (click.isRightClick() && startTime < 10)
                    eventData.setStartTime(startTime + 1);
                break;
            case 15:
                int minPlayer = eventData.getMinPlayer();

                if (click.isLeftClick() && minPlayer > 2)
                    eventData.setMinPlayer(minPlayer - 1);
                else if (click.isRightClick() && minPlayer < 15)
                    eventData.setMinPlayer(minPlayer + 1);
                break;
            case 16:
                int maxPlayer = eventData.getMaxPlayer();

                if (click.isLeftClick() && maxPlayer > 20)
                    eventData.setMaxPlayer(maxPlayer - 1);
                else if (click.isRightClick() && maxPlayer < 100)
                    eventData.setMaxPlayer(maxPlayer + 1);
                break;
        }

        update();
    }

}
