package tech.sebazcrc.permadeath.nms.v1_16_R3.block;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.EntityType;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.TileEntityMobSpawner;
import tech.sebazcrc.permadeath.util.interfaces.ElementalSpawner;
import tech.sebazcrc.permadeath.util.lib.ElementalType;

public class ElementalSpawnerImpl implements ElementalSpawner {

    @Override
    public void placeCustomBlock(Location pos, ElementalType type) {
        Block o = pos.getBlock();
        o.setType(Material.SPAWNER);

        BlockPosition bp = new BlockPosition(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
        TileEntityMobSpawner spawner = (TileEntityMobSpawner) ((CraftWorld) pos.getWorld()).getHandle()
                .getTileEntity(bp);

        NBTTagCompound tileData = spawner.b();
        NBTTagCompound spawnData = new NBTTagCompound();
        NBTTagList armor = new NBTTagList();
        NBTTagCompound head = new NBTTagCompound();
        NBTTagCompound tags = new NBTTagCompound();

        head.setString("id", "minecraft:jigsaw");
        head.setByte("Count", (byte) 1);
        head.set("tag", tags);

        armor.add(new NBTTagCompound());
        armor.add(new NBTTagCompound());
        armor.add(new NBTTagCompound());
        armor.add(head);

        spawnData.set("ArmorItems", armor);
        tileData.setBoolean("ElementalSpawner", true);

        tags.setInt("ElementalType", getElementalTypeNumber(type));

        spawnData.setString("id", "minecraft:armor_stand");
        spawnData.setInt("Invisible", 1);
        spawnData.setByte("Marker", (byte) 1);

        tileData.setShort("SpawnRange", (short) 0);
        tileData.setShort("SpawnCount", (short) 0);
        tileData.setShort("RequiredPlayerRange", (short) 0);
        tileData.setShort("MaxNearbyEntities", (short) 0);
        tileData.set("SpawnData", spawnData);

        spawner.load(spawner.getBlock(), tileData);

        pos.getWorld().playSound(pos, Sound.BLOCK_NETHER_BRICKS_BREAK, 1, 1);
    }

    @Override
    public boolean isElementalSpawner(Location pos) {
        BlockPosition bp = new BlockPosition(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
        TileEntityMobSpawner spawner = (TileEntityMobSpawner) ((CraftWorld) Objects.requireNonNull(pos.getWorld()))
                .getHandle().getTileEntity(bp);

        assert spawner != null;
        NBTTagCompound tileData = spawner.b();
        boolean dataFound = tileData != null && tileData.hasKey("ElementalSpawner")
                && tileData.getBoolean("ElementalSpawner");

        if (!dataFound) {
            if (pos.getBlock().getState() instanceof CreatureSpawner) {
                CreatureSpawner c = (CreatureSpawner) pos.getBlock().getState();
                if (c.getSpawnedType() == EntityType.ARMOR_STAND) {
                    dataFound = true;
                }
            }
        }

        return dataFound;
    }

    private int getElementalTypeNumber(ElementalType type) {
        int typeNumber = 0;

        if (type == ElementalType.AIR) {
            typeNumber = 1;
        } else if (type == ElementalType.EARTH) {
            typeNumber = 2;
        } else if (type == ElementalType.FIRE) {
            typeNumber = 3;
        } else if (type == ElementalType.WATER) {
            typeNumber = 4;
        } else {
            typeNumber = 5;
        }

        return typeNumber;
    }

}
