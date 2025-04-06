package me.rejomy.deathanimation.listener;

import com.github.retrooper.packetevents.protocol.player.User;
import lombok.RequiredArgsConstructor;
import me.rejomy.deathanimation.Main;
import me.rejomy.deathanimation.config.Config;
import me.rejomy.deathanimation.util.PEUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class PlayerDeathListener implements Listener {

    private final Config config;
    private final Main main;
    private int nextEntityId = 6000;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!inTheAllowedWorld(player.getWorld()) || !PEUtil.isOnline(player))
            return;

        Location loc = player.getLocation();
        List<Player> bukkitRecipients = player.getWorld().getPlayers().stream().filter(recipient -> {
            double distance = player.getLocation().distance(recipient.getLocation());
            return recipient != player && distance <= config.getMaxDistanceToEntity();
        }).toList();
        List<User> recipients = PEUtil.getUsers(bukkitRecipients);
        int entityId = ++nextEntityId;
        UUID uuid = UUID.randomUUID();

        PEUtil.spawnFakePlayer(recipients, player, uuid, entityId, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        if (config.isCopyEntityEquipment())
            PEUtil.sendEquipmentPacket(recipients, player, entityId);
        PEUtil.sendDeathMetadata(recipients, entityId);
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> PEUtil.removeFakePlayer(recipients, uuid, entityId), config.getRemoveEntityTicks());
    }

    private boolean inTheAllowedWorld(World world) {
        return config.getEnabledWorlds().contains(world);
    }
}
