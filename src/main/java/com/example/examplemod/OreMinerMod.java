package com.example.examplemod;

import com.example.examplemod.commands.MiningCommand;
import com.example.examplemod.config.MinerConfig;
import com.example.examplemod.mining.AutoMiner;
import com.example.examplemod.mining.OreDetector;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = OreMinerMod.MODID, version = OreMinerMod.VERSION, clientSideOnly = true)
public class OreMinerMod
{
    public static final String MODID = "oreminer";
    public static final String VERSION = "1.0";

    private static OreMinerMod instance;
    private OreDetector oreDetector;
    private AutoMiner autoMiner;
    private MinerConfig config;

    public OreMinerMod() {
        instance = this;
    }

    public static OreMinerMod getInstance() {
        return instance;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize config
        config = new MinerConfig();
        System.out.println("[OreMiner] Pre-initialization complete");
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // Initialize components
        oreDetector = new OreDetector();
        autoMiner = new AutoMiner(oreDetector);

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(oreDetector);
        MinecraftForge.EVENT_BUS.register(autoMiner);

        // Register commands
        ClientCommandHandler.instance.registerCommand(new MiningCommand());

        System.out.println("[OreMiner] Mod initialized successfully!");
        System.out.println("[OreMiner] Type /89 to open the configuration GUI");
        System.out.println("[OreMiner] Type /89 toggle to enable/disable mining");
        System.out.println("[OreMiner] Type /89 help for more information");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !Minecraft.getMinecraft().isGamePaused()) {
            // Run main mod logic on client tick
            if (autoMiner.isEnabled()) {
                autoMiner.onTick();
            }
        }
    }

    public OreDetector getOreDetector() {
        return oreDetector;
    }

    public AutoMiner getAutoMiner() {
        return autoMiner;
    }

    public MinerConfig getConfig() {
        return config;
    }
}