package de.hglabor.worldfeatures.utils.gui;

import de.hglabor.worldfeatures.utils.gui.button.GuiButton;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class GuiBuilder {

    private String name;
    private int slots;
    private Plugin plugin;
    private HashMap<Integer, ItemStack> items;
    private HashMap<Integer, GuiButton> guiButtons;
    private ArrayList<Listener> waitingForUnRegister = new ArrayList<>();

    public GuiBuilder(Plugin plugin) {
        this.name = "Gui Title";
        this.slots = 9;
        items = new HashMap<>();
        guiButtons = new HashMap<>();
        this.plugin = plugin;
    }

    public GuiBuilder withItem(ItemStack itemStack, int slot) {
        items.put(slot, itemStack);
        return this;
    }

    public GuiBuilder withButton(int slot, GuiButton guiButton) {
        guiButtons.put(slot, guiButton);
        items.put(slot, guiButton.getItemStackResult());
        return this;
    }

    public GuiBuilder withSlots(int slots) {
        this.slots = slots;
        return this;
    }

    public GuiBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public Inventory build() {
        Inventory inventory = Bukkit.createInventory(null, this.slots, this.name);
        for (int i : items.keySet()) {
            inventory.setItem(i, items.get(i));
            Listener listener = new Listener() {
                @EventHandler
                public void onInventoryClick(InventoryClickEvent event) {
                    if(event.getClickedInventory() != null) {
                        if(event.getView().getTitle().equalsIgnoreCase(name)) {
                            event.setCancelled(true);
                            if(event.getCurrentItem() != null) {
                                if(event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(items.get(i).getItemMeta().getDisplayName())) {
                                    if(guiButtons.containsKey(i)) {
                                        GuiButton guiButton = guiButtons.get(i);
                                        guiButton.press(event);
                                    }
                                    //waitingForUnRegister.remove(this);
                                    //HandlerList.unregisterAll(this);
                                }
                            }
                        }
                    }
                }
            };
            Bukkit.getPluginManager().registerEvents(listener, plugin);
            waitingForUnRegister.add(listener);
            //UNREGISTER ON CLOSE
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onCloseInventory(InventoryCloseEvent event) {
                    if(event.getView().getTitle().equalsIgnoreCase(name)) {
                        for (Listener listeners : List.copyOf(waitingForUnRegister)) {
                            HandlerList.unregisterAll(listeners);
                            waitingForUnRegister.remove(listeners);
                        }
                    }
                }
            }, plugin);
        }
        return inventory;
    }


}
