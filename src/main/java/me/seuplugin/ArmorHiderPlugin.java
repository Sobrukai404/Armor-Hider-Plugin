package me.seuplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.Pair;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ArmorHiderPlugin extends JavaPlugin {

    private final Map<UUID, Set<EquipmentSlot>> hiddenArmor = new HashMap<>();

    @Override
    public void onEnable() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        manager.addPacketListener(new PacketAdapter(this, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int entityId = event.getPacket().getIntegers().read(0);
                Player viewer = event.getPlayer();
                Player target = null;

                for (Player p : getServer().getOnlinePlayers()) {
                    if (p.getEntityId() == entityId) {
                        target = p;
                        break;
                    }
                }

                if (target == null) return;

                Set<EquipmentSlot> hidden = hiddenArmor.getOrDefault(target.getUniqueId(), Set.of());

                List<Pair<ItemSlot, ItemStack>> original = event.getPacket().getSlotStackPairLists().read(0);
                List<Pair<ItemSlot, ItemStack>> modified = new ArrayList<>();

                for (Pair<ItemSlot, ItemStack> pair : original) {
                    EquipmentSlot slot = convertToBukkitSlot(pair.getFirst());
                    if (hidden.contains(slot)) {
                        modified.add(new Pair<>(pair.getFirst(), new ItemStack(Material.AIR)));
                    } else {
                        modified.add(pair);
                    }
                }

                event.getPacket().getSlotStackPairLists().write(0, modified);
            }
        });

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onAnyEvent(PlayerEvent event) {
                Player player = event.getPlayer();
                if (hiddenArmor.containsKey(player.getUniqueId())) {
                    Bukkit.getScheduler().runTaskLater(ArmorHiderPlugin.this, () -> refresh(player), 1L);
                }
            }

            @EventHandler
            public void onDamage(EntityDamageEvent event) {
                if (event.getEntity() instanceof Player player && hiddenArmor.containsKey(player.getUniqueId())) {
                    Bukkit.getScheduler().runTaskLater(ArmorHiderPlugin.this, () -> refresh(player), 1L);
                }
            }

            @EventHandler
            public void onInteract(PlayerInteractEvent event) {
                Player player = event.getPlayer();
                if (hiddenArmor.containsKey(player.getUniqueId())) {
                    Bukkit.getScheduler().runTaskLater(ArmorHiderPlugin.this, () -> refresh(player), 1L);
                }
            }
        }, this);

        getCommand("esconder").setExecutor((sender, cmd, label, args) -> {
            try {
                if (!(sender instanceof Player p)) return true;

                if (args.length < 1) {
                    p.sendMessage("§cUso: /esconder <capacete|peitoral|calça|bota|tudo>");
                    return true;
                }

                Set<EquipmentSlot> slots = hiddenArmor.computeIfAbsent(p.getUniqueId(), k -> new HashSet<>());

                switch (args[0].toLowerCase()) {
                    case "capacete" -> slots.add(EquipmentSlot.HEAD);
                    case "peitoral" -> slots.add(EquipmentSlot.CHEST);
                    case "calça" -> slots.add(EquipmentSlot.LEGS);
                    case "bota" -> slots.add(EquipmentSlot.FEET);
                    case "tudo" -> slots.addAll(List.of(
                            EquipmentSlot.HEAD,
                            EquipmentSlot.CHEST,
                            EquipmentSlot.LEGS,
                            EquipmentSlot.FEET));
                    default -> {
                        p.sendMessage("§cParte inválida.");
                        return true;
                    }
                }

                refresh(p);
                p.sendMessage("§aArmadura escondida.");
            } catch (Exception ignored) {
            }
            return true;
        });

        getCommand("mostrar").setExecutor((sender, cmd, label, args) -> {
            try {
                if (!(sender instanceof Player p)) return true;
                hiddenArmor.remove(p.getUniqueId());
                refresh(p);
                p.sendMessage("§aSua armadura agora está visível.");
            } catch (Exception ignored) {
            }
            return true;
        });
    }

    private EquipmentSlot convertToBukkitSlot(ItemSlot slot) {
        return switch (slot) {
            case HEAD -> EquipmentSlot.HEAD;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS -> EquipmentSlot.LEGS;
            case FEET -> EquipmentSlot.FEET;
            case MAINHAND -> EquipmentSlot.HAND;
            case OFFHAND -> EquipmentSlot.OFF_HAND;
            default -> EquipmentSlot.HAND;
        };
    }

    private void refresh(Player p) {
        Set<EquipmentSlot> slots = hiddenArmor.getOrDefault(p.getUniqueId(), Set.of());

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack toSend = p.getInventory().getItem(slot);

            if (slots.contains(slot)) {
                toSend = new ItemStack(Material.AIR);
            }

            for (Player viewer : Bukkit.getOnlinePlayers()) {
                viewer.sendEquipmentChange(p, slot, toSend);
            }
        }

        Bukkit.getScheduler().runTaskLater(this, p::updateInventory, 1L);
    }

    private void removeArmorAttributes(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            Multimap<Attribute, AttributeModifier> modifiersMap = meta.getAttributeModifiers();

            if (modifiersMap == null) continue;

            for (Attribute attribute : modifiersMap.keySet()) {
                meta.removeAttributeModifier(attribute);
            }

            item.setItemMeta(meta);
            player.getInventory().setItem(slot, item);
        }
    }
}
