package dev.nandi0813.practice.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherChange implements Listener {

    @EventHandler ( priority = EventPriority.HIGHEST )
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) event.setCancelled(true);
    }

    @EventHandler ( priority = EventPriority.HIGHEST )
    public void onThunderChange(ThunderChangeEvent event) {
        if (event.toThunderState()) event.setCancelled(true);
    }

}
