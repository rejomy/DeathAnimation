package me.rejomy.deathanimation.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.*;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes.FLOAT;

@UtilityClass
public class PEUtil {

    private final int HEALTH_METADATA_NUMBER;

    static {
        ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
        HEALTH_METADATA_NUMBER = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17) ? 9 :
                (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14) ? 8 :
                        (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_10) ? 7 : 6));
    }

    public void spawnFakePlayer(List<Player> recipients, Player player, UUID uuid, int entityId, double x, double y, double z, float yaw, float pitch) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        String name = player.getName();
        UserProfile profile = new UserProfile(uuid, name, user.getProfile().getTextureProperties());

        WrapperPlayServerPlayerInfo playerInfo = new WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.ADD_PLAYER,
                new WrapperPlayServerPlayerInfo.PlayerData(
                        Component.text(name),
                        profile,
                        GameMode.SURVIVAL,
                        0
                )
        );

        List<EntityData> metadata = new ArrayList<>();
        metadata.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0));
        metadata.add(new EntityData(2, EntityDataTypes.STRING, name));
        metadata.add(new EntityData(3, EntityDataTypes.BOOLEAN, true));
        metadata.add(new EntityData(HEALTH_METADATA_NUMBER, EntityDataTypes.FLOAT, 0.0F));
        metadata.add(new EntityData(10, EntityDataTypes.INT, 0));
        metadata.add(new EntityData(11, EntityDataTypes.BOOLEAN, false));
        metadata.add(new EntityData(12, EntityDataTypes.INT, 0));
        metadata.add(new EntityData(13, EntityDataTypes.INT, 0));

        WrapperPlayServerSpawnPlayer spawn = new WrapperPlayServerSpawnPlayer(
                entityId,
                uuid,
                new Location(x, y, z, yaw, pitch),
                metadata
        );

        recipients.forEach(recipient -> {
            User recipientUser = PacketEvents.getAPI().getPlayerManager().getUser(recipient);
            recipientUser.sendPacket(playerInfo);
            recipientUser.sendPacket(spawn);
        });
    }

    public void sendDeathMetadata(List<Player> recipients, int entityId) {
        List<EntityData> metadata = new ArrayList<>();
        metadata.add(new EntityData(HEALTH_METADATA_NUMBER, FLOAT, 0.0F));

        WrapperPlayServerEntityMetadata metaPacket = new WrapperPlayServerEntityMetadata(
                entityId,
                metadata
        );

        recipients.forEach(recipient -> PacketEvents.getAPI().getPlayerManager().getUser(recipient).sendPacket(metaPacket));
    }

    public void removeFakePlayer(List<Player> recipients, UUID uuid, int entityId) {
        WrapperPlayServerDestroyEntities destroy = new WrapperPlayServerDestroyEntities(entityId);
        WrapperPlayServerPlayerInfo.PlayerData data = new WrapperPlayServerPlayerInfo.PlayerData(null, new UserProfile(uuid, null), GameMode.SURVIVAL, 0);
        WrapperPlayServerPlayerInfo playerInfo = new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER, data);

        recipients.forEach(recipient -> {
            if (recipient.isOnline()) {
                User user = PacketEvents.getAPI().getPlayerManager().getUser(recipient);
                user.sendPacket(destroy);
                user.sendPacket(playerInfo);
            }
        });
    }

    public static void sendEquipmentPacket(List<Player> recipients, Player source, int entityId) {
        List<Equipment> equipmentList = new ArrayList<>();

        ItemStack mainHand = SpigotConversionUtil.fromBukkitItemStack(source.getInventory().getItemInHand());
        if (mainHand != null && mainHand.getType() != ItemTypes.AIR)
            equipmentList.add(new Equipment(EquipmentSlot.MAIN_HAND, mainHand));

        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_12)) {
            ItemStack offHand = SpigotConversionUtil.fromBukkitItemStack(source.getInventory().getItemInOffHand());
            equipmentList.add(new Equipment(EquipmentSlot.OFF_HAND, offHand));
        }

        ItemStack boots =  SpigotConversionUtil.fromBukkitItemStack(source.getInventory().getBoots());
        ItemStack leggings =  SpigotConversionUtil.fromBukkitItemStack(source.getInventory().getLeggings());
        ItemStack chestplate =  SpigotConversionUtil.fromBukkitItemStack(source.getInventory().getChestplate());
        ItemStack helmet =  SpigotConversionUtil.fromBukkitItemStack(source.getInventory().getHelmet());

        if (boots != null)
            equipmentList.add(new Equipment(EquipmentSlot.BOOTS, boots));
        if (leggings != null)
            equipmentList.add(new Equipment(EquipmentSlot.LEGGINGS, leggings));
        if (chestplate != null)
            equipmentList.add(new Equipment(EquipmentSlot.CHEST_PLATE, chestplate));
        if (helmet != null)
            equipmentList.add(new Equipment(EquipmentSlot.HELMET, helmet));

        WrapperPlayServerEntityEquipment equipmentWrapper = new WrapperPlayServerEntityEquipment(entityId, equipmentList);

        recipients.forEach(recipient -> {
            if (recipient.isOnline()) {
                User user = PacketEvents.getAPI().getPlayerManager().getUser(recipient);
                user.sendPacket(equipmentWrapper);
            }
        });
    }
}
