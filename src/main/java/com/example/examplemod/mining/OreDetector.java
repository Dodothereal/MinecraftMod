package com.example.examplemod.mining;

import com.example.examplemod.OreMinerMod;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OreDetector {

    private final Map<String, Block> oreBlocks = new HashMap<>();
    private final List<BlockPos> detectedOres = new ArrayList<>();
    private int scanTicks = 0;
    private final int SCAN_FREQUENCY = 5; // Scan every 5 ticks

    public OreDetector() {
        // Initialize ore block map
        oreBlocks.put("Diamond", Blocks.diamond_ore);
        oreBlocks.put("Emerald", Blocks.emerald_ore);
        oreBlocks.put("Gold", Blocks.gold_ore);
        oreBlocks.put("Iron", Blocks.iron_ore);
        oreBlocks.put("Coal", Blocks.coal_ore);
        oreBlocks.put("Redstone", Blocks.redstone_ore);
        oreBlocks.put("Lapis", Blocks.lapis_ore);

        System.out.println("[OreMiner] OreDetector initialized with " + oreBlocks.size() + " ore types");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END &&
                !Minecraft.getMinecraft().isGamePaused() &&
                OreMinerMod.getInstance().getAutoMiner().isEnabled()) {

            scanTicks++;
            if (scanTicks >= SCAN_FREQUENCY) {
                scanTicks = 0;
                scanForOres();
            }
        }
    }

    private void scanForOres() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        int previousCount = detectedOres.size();
        detectedOres.clear();

        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        int range = OreMinerMod.getInstance().getConfig().getRange();

        // Scan blocks in range
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);

                    if (isOre(pos, mc.theWorld)) {
                        detectedOres.add(pos);
                    }
                }
            }
        }

        // Only log if the count changed to avoid spam
        if (detectedOres.size() != previousCount) {
            System.out.println("[OreMiner] Scan completed: Found " + detectedOres.size() + " ores in range " + range);
        }
    }

    private boolean isOre(BlockPos pos, World world) {
        Block block = world.getBlockState(pos).getBlock();

        for (Map.Entry<String, Block> entry : oreBlocks.entrySet()) {
            if (entry.getValue() == block &&
                    OreMinerMod.getInstance().getConfig().isOreEnabled(entry.getKey())) {
                return true;
            }
        }

        return false;
    }

    public BlockPos getNearestOre() {
        if (detectedOres.isEmpty()) {
            return null;
        }

        Minecraft mc = Minecraft.getMinecraft();
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (BlockPos pos : detectedOres) {
            double distance = pos.distanceSq(playerPos);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = pos;
            }
        }

        return nearest;
    }

    public String getOreTypeName(BlockPos pos) {
        if (pos == null || Minecraft.getMinecraft().theWorld == null) {
            return "Unknown";
        }

        Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();

        for (Map.Entry<String, Block> entry : oreBlocks.entrySet()) {
            if (entry.getValue() == block) {
                return entry.getKey();
            }
        }

        return "Unknown";
    }
}