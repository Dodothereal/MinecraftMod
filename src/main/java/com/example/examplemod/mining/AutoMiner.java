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
            miningState = MiningState.MINING;
        }
    }

    private void mineTarget() {
        Minecraft mc = Minecraft.getMinecraft();

        if (targetOre == null) {
            miningState = MiningState.SEARCHING;
            return;
        }

        // Start mining the block
        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,
                targetOre,
                EnumFacing.DOWN)); // Direction doesn't matter much

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

        // Check if block is still there
        Block block = mc.theWorld.getBlockState(targetOre).getBlock();
        if (block.isAir(mc.theWorld, targetOre)) {
            // Block is broken, search for next ore
            miningState = MiningState.SEARCHING;
            return;
        }

        // Break the block after a period of waiting
        // This simulates breaking the block after mining it
        if (miningTicks >= 20) { // 1 second at 20 ticks/second
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                    targetOre,
                    EnumFacing.DOWN));

            // Move back to searching
            miningState = MiningState.SEARCHING;
        }
    }
}