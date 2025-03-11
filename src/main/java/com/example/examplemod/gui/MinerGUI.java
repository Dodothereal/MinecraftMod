package com.example.examplemod.gui;

import com.example.examplemod.OreMinerMod;
import com.example.examplemod.config.MinerConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MinerGUI extends GuiScreen {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/demo_background.png");
    private int guiLeft;
    private int guiTop;
    private final int xSize = 248;
    private final int ySize = 166;

    private GuiButton toggleButton;
    private List<OreToggleButton> oreButtons = new ArrayList<>();
    private GuiButton humanizeButton;
    private GuiButton rangeButton;

    @Override
    public void initGui() {
        super.initGui();

        System.out.println("[OreMiner] Initializing GUI");

        // Calculate GUI position (center of screen)
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        // Clear existing buttons
        this.buttonList.clear();

        // Add buttons
        MinerConfig config = OreMinerMod.getInstance().getConfig();
        boolean isEnabled = OreMinerMod.getInstance().getAutoMiner().isEnabled();

        // Toggle button
        this.buttonList.add(toggleButton = new GuiButton(0, guiLeft + 10, guiTop + 10, 100, 20,
                isEnabled ? "Disable Miner" : "Enable Miner"));

        // Ore toggle buttons
        String[] oreTypes = {"Diamond", "Emerald", "Gold", "Iron", "Coal", "Redstone", "Lapis"};
        for (int i = 0; i < oreTypes.length; i++) {
            OreToggleButton button = new OreToggleButton(
                    i + 1,
                    guiLeft + 10,
                    guiTop + 40 + (i * 16),
                    120,
                    14,
                    oreTypes[i],
                    config.isOreEnabled(oreTypes[i])
            );
            this.buttonList.add(button);
            oreButtons.add(button);
        }

        // Humanize rotations button
        this.buttonList.add(humanizeButton = new GuiButton(
                8,
                guiLeft + 140,
                guiTop + 40,
                100,
                20,
                "Humanize: " + (config.isHumanizeEnabled() ? "ON" : "OFF")
        ));

        // Range slider button
        this.buttonList.add(rangeButton = new GuiButton(
                9,
                guiLeft + 140,
                guiTop + 70,
                100,
                20,
                "Range: " + config.getRange()
        ));

        System.out.println("[OreMiner] GUI initialized with " + this.buttonList.size() + " buttons");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw background
        this.mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Draw title
        this.fontRendererObj.drawString("Ore Miner Configuration", guiLeft + 10, guiTop + 5, 0x404040);

        // Draw ore section title
        this.fontRendererObj.drawString("Target Ores:", guiLeft + 10, guiTop + 35, 0x404040);

        // Draw settings section title
        this.fontRendererObj.drawString("Settings:", guiLeft + 140, guiTop + 35, 0x404040);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        System.out.println("[OreMiner] Button clicked: " + button.id);

        MinerConfig config = OreMinerMod.getInstance().getConfig();

        if (button == toggleButton) {
            // Toggle miner on/off
            OreMinerMod.getInstance().getAutoMiner().toggle();
            boolean isEnabled = OreMinerMod.getInstance().getAutoMiner().isEnabled();
            toggleButton.displayString = isEnabled ? "Disable Miner" : "Enable Miner";
        } else if (button.id >= 1 && button.id <= 7) {
            // Toggle ore enabled status
            OreToggleButton oreButton = oreButtons.get(button.id - 1);
            oreButton.toggle();
            config.setOreEnabled(oreButton.getOreName(), oreButton.isEnabled());
            System.out.println("[OreMiner] Toggled " + oreButton.getOreName() + " mining: " + oreButton.isEnabled());
        } else if (button == humanizeButton) {
            // Toggle humanize setting
            boolean humanize = !config.isHumanizeEnabled();
            config.setHumanizeEnabled(humanize);
            humanizeButton.displayString = "Humanize: " + (humanize ? "ON" : "OFF");
            System.out.println("[OreMiner] Humanize setting: " + (humanize ? "ON" : "OFF"));
        } else if (button == rangeButton) {
            // Cycle through range options (4, 5, 6)
            int currentRange = config.getRange();
            int newRange = (currentRange % 3) + 4; // Cycle through 4, 5, 6
            config.setRange(newRange);
            rangeButton.displayString = "Range: " + newRange;
            System.out.println("[OreMiner] Range setting: " + newRange);
        }

        config.saveConfig();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // Custom button for ore toggles
    private static class OreToggleButton extends GuiButton {
        private final String oreName;
        private boolean enabled;

        public OreToggleButton(int buttonId, int x, int y, int width, int height, String oreName, boolean enabled) {
            super(buttonId, x, y, width, height, "");
            this.oreName = oreName;
            this.enabled = enabled;
            updateDisplayString();
        }

        public void toggle() {
            this.enabled = !this.enabled;
            updateDisplayString();
        }

        private void updateDisplayString() {
            this.displayString = oreName + ": " + (enabled ? "✓" : "✗");
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getOreName() {
            return oreName;
        }
    }
}