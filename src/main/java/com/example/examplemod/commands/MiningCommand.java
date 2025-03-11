package com.example.examplemod.commands;

import com.example.examplemod.OreMinerMod;
import com.example.examplemod.gui.MinerGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class MiningCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "89"; // Command will be /89
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/89 - Opens the ore miner configuration GUI";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true; // Anyone can use this client-side command
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            // Open GUI when no arguments are provided
            Minecraft.getMinecraft().displayGuiScreen(new MinerGUI());
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("toggle")) {
                // Toggle miner on/off
                OreMinerMod.getInstance().getAutoMiner().toggle();
                boolean isEnabled = OreMinerMod.getInstance().getAutoMiner().isEnabled();
                sender.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GREEN + "Auto miner " +
                                (isEnabled ? "enabled" : "disabled")));
            } else if (args[0].equalsIgnoreCase("help")) {
                // Display help info
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "==== OreMiner Help ===="));
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "/89 - Open configuration GUI"));
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "/89 toggle - Toggle mining on/off"));
            }
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // No permission level required (client-side command)
    }
}