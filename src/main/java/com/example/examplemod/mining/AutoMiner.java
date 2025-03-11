package com.example.examplemod.mining;

import com.example.examplemod.OreMinerMod;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

public class AutoMiner {

    private final OreDetector oreDetector;
    private boolean enabled = false;
    private BlockPos targetOre = null;
    private MiningState miningState = MiningState.SEARCHING;
    private int miningTicks = 0;
    private final Random random = new Random();
    private int breakingProgress = 0;

    private enum MiningState {
        SEARCHING,
        ROTATING,
        MINING,
        WAITING
    }

    public AutoMiner(OreDetector oreDetector) {
        this.oreDetector = oreDetector;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
        this.miningState = MiningState.SEARCHING;
        System.out.println("[OreMiner] Mining toggled: " + (this.enabled ? "ON" : "OFF"));
    }

    public void onTick() {
        switch (miningState) {
            case SEARCHING:
                searchForOre();
                break;
            case ROTATING:
                rotateToTarget();
                break;
            case MINING:
                mineTarget();
                break;
            case WAITING:
                waitForBlockBreak();
                break;
        }
    }

    private void searchForOre() {
        targetOre = oreDetector.getNearestOre();

        if (targetOre != null) {
            // Found an ore, switch to rotating state
            System.out.println("[OreMiner] Found ore at " + targetOre.toString() + ", type: " + oreDetector.getOreTypeName(targetOre));
            miningState = MiningState.ROTATING;

            // Select pickaxe
            selectBestPickaxe();
        }
    }

    private void selectBestPickaxe() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        InventoryPlayer inventory = player.inventory;

        // Check if player is already holding a pickaxe
        ItemStack currentItem = inventory.getCurrentItem();
        if (currentItem != null && currentItem.getItem() instanceof ItemPickaxe) {
            return;
        }

        // Find a pickaxe in hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemPickaxe) {
                inventory.currentItem = i;
                return;
            }
        }
    }

    private void rotateToTarget() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (targetOre == null) {
            miningState = MiningState.SEARCHING;
            return;
        }

        // Calculate target rotation
        double dx = targetOre.getX() + 0.5 - player.posX;
        double dy = targetOre.getY() + 0.5 - (player.posY + player.getEyeHeight());
        double dz = targetOre.getZ() + 0.5 - player.posZ;

        double distance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distance));

        // Apply humanization if enabled
        if (OreMinerMod.getInstance().getConfig().isHumanizeEnabled()) {
            yaw += (random.nextFloat() - 0.5f) * 2.0f;
            pitch += (random.nextFloat() - 0.5f) * 1.0f;
        }

        // Normalize angles
        yaw = MathHelper.wrapAngleTo180_float(yaw);
        pitch = MathHelper.clamp_float(pitch, -90.0F, 90.0F);

        // Set player rotation
        player.rotationYaw = yaw;
        player.rotationPitch = pitch;

        // Check if we're looking at the target
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                mop.getBlockPos().equals(targetOre)) {
            System.out.println("[OreMiner] Now looking at target, starting to mine");
            miningState = MiningState.MINING;
        }
    }

    private void mineTarget() {
        Minecraft mc = Minecraft.getMinecraft();

        if (targetOre == null) {
            miningState = MiningState.SEARCHING;
            return;
        }

        // Reset breaking progress
        breakingProgress = 0;

        // Start mining the block
        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,
                targetOre,
                EnumFacing.DOWN));

        // Add this line to trigger the client-side animation
        mc.playerController.onPlayerDamageBlock(targetOre, EnumFacing.DOWN);

        // Switch to waiting state
        miningState = MiningState.WAITING;
        miningTicks = 0;
    }

    private void waitForBlockBreak() {
        Minecraft mc = Minecraft.getMinecraft();

        if (targetOre == null) {
            miningState = MiningState.SEARCHING;
            return;
        }

        miningTicks++;

        // Check if block is still there (using isAirBlock for more reliable detection)
        if (mc.theWorld.isAirBlock(targetOre)) {
            // Block is broken, search for next ore
            System.out.println("[OreMiner] Block broken, searching for next target");
            miningState = MiningState.SEARCHING;
            return;
        }

        // Update breaking animation every few ticks
        if (miningTicks % 4 == 0) {
            // This continuously damages the block to show breaking progress
            mc.playerController.onPlayerDamageBlock(targetOre, EnumFacing.DOWN);
            breakingProgress++;
            System.out.println("[OreMiner] Breaking progress: " + breakingProgress);
        }

        // Break the block after a period of waiting
        if (miningTicks >= 20) { // 1 second at 20 ticks/second
            System.out.println("[OreMiner] Sending stop digging packet");
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                    targetOre,
                    EnumFacing.DOWN));

            // Move back to searching state regardless
            miningState = MiningState.SEARCHING;
        }
    }
}