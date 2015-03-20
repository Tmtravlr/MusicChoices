package com.tmtravlr.musicchoices;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import paulscode.sound.SoundSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Blocks;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Main mod file! 
 * 
 * Note that this mod should only ever be present on the Client side or it will cause crashes!
 * 
 * @author Rebeca Rey
 * @Date Febuary 2015 
 */
@Mod(modid = MusicChoicesMod.MODID, version = MusicChoicesMod.VERSION)
public class MusicChoicesMod
{
	
    public static final String MODID = "musicchoices";
    public static final String VERSION = "1.0";
    
    @Instance(MODID)
	public static MusicChoicesMod musicChoices;

	@SidedProxy(
			clientSide = "com.tmtravlr.musicchoices.ClientProxy",
			serverSide = "com.tmtravlr.musicchoices.CommonProxy"
			)
	public static CommonProxy proxy;
    private static Minecraft mc = Minecraft.getMinecraft();
    
    //Debug options
    public static boolean debug = false;
    public static boolean super_duper_debug = false;
    
    //Handles what music should play; based on the MusicTicker class.
    public static MusicChoicesMusicTicker ticker = new MusicChoicesMusicTicker(mc);
    
    //When an achievement gets set to true in this map,
  	//the corresponding achievement music will try to play
  	public static Map<Achievement, Boolean> achievementsUnlocked = new HashMap();
  	public static boolean worldLoaded = false;
  	
  	public static int fadeStrength = 10;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	musicChoices = this;
    	
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		
		//Load the debug
		config.load();
		
		debug = config.getBoolean("debug", "debug", false, "Turns on regular debug output.");
		super_duper_debug = config.getBoolean("in-depth_debug", "debug", false, "Turns on more in-depth debug output.");
		
		config.save();
		
		proxy.registerEventHandlers();
		proxy.registerTickHandlers();
		proxy.registerResourceReloadListeners();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	
    	//Replace the vanilla music ticker with our custom one
    	
    	try {
    		for(Field f : mc.getClass().getDeclaredFields()) {
    			if(f.getName().equals("mcMusicTicker") || f.getName().equals("field_147126_aw")) {
    				if(debug) System.out.println("[Music Choices] Found music ticker in Minecraft class.");
    				f.setAccessible(true);
    				f.set(mc, ticker);
    			}
    		}
    	}
    	catch(Exception e) {
    		throw new ReportedException(new CrashReport("Music Choices couldn't load in it's music ticker! Things won't work. =( Better let Tmtravlr know.", e));
    	}
        
    }
    
    public static boolean isGamePaused() {
    	return mc.isSingleplayer() && mc.currentScreen != null && mc.currentScreen.doesGuiPauseGame() && !(mc.getIntegratedServer() != null && mc.getIntegratedServer().getPublic());
    }
}
