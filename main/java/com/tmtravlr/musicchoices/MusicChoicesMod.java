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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Main mod file! 
 * 
 * Note that this mod should only ever be present on the Client side or it will cause crashes!
 * 
 * @author Rebeca Rey
 * @Date Febuary 2015 
 */
@Mod(modid = MusicChoicesMod.MODID, version = MusicChoicesMod.VERSION, name = "Music Choices")
public class MusicChoicesMod
{
	
    public static final String MODID = "musicchoices";
    public static final String VERSION = "1.2_for_1.7.10";
    
    @Instance(MODID)
	public static MusicChoicesMod musicChoices;

    //Debug options
    public static boolean debug = false;
    public static boolean super_duper_debug = false;
    
    
    /***** Options! ******/
	
    public static boolean overrideJsonOptions = false;

	/** Maximum number of "background" tracks that can play at once. */
	public static int maxBackground = 3;
	
	/** Maximum number of "overtop" tracks that can play at once that don't have overlap set to true. */
	public static int maxOvertop = 1;
	
	/** How much the background music should fade when music plays over top of it. */
	public static float backgroundFade = 0.4f;
	
	/** How fast the background music fades */
	public static int fadeStrength = 10;
	
	/** Tick delay for the menu music */
	public static int menuTickDelayMin = 20;
	public static int menuTickDelayMax = 600;
	
	/** Tick delay for all ingame music */
	public static int ingameTickDelayMin = 1200;
	public static int ingameTickDelayMax = 3600;
	
	/** Play vanilla tracks */
	public static boolean playVanilla = true;
	
	/** Stop tracks that no longer apply */
	public static boolean stopTracks = true;
	
	/** Distance to stop battle music */
	public static int battleDistance = 16;
	
	/** if this should play for "non-monster" entities */
	public static boolean battleMonsterOnly = true;
	
	/************************/
  	
    
    //When an achievement gets set to true in this map,
  	//the corresponding achievement music will try to play
  	public static Map<Achievement, Boolean> achievementsUnlocked = new HashMap();
  	public static boolean worldLoaded = false;
  	
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	if(event.getSide() == Side.SERVER) {
    		return; //Don't load on the server
    	}
    	
    	musicChoices = this;
    	
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		
		//Load the debug
		config.load();
		
		//debug
		
		debug = config.getBoolean("debug", "debug", false, "Turns on regular debug output.");
		super_duper_debug = config.getBoolean("in-depth_debug", "debug", false, "Turns on more in-depth debug output. (Warning! This will spam your console!)");
		
		//options
		
		overrideJsonOptions = config.getBoolean("override json options", "options", false, "Set to true to override the options loaded in through the sounds.json files with these options.");
		
		maxBackground = config.getInt("maximum background tracks", "options", 3, 1, 10, "The maximum number of background tracks that can play at once (only one will be at full volume at a time).");
		maxOvertop = config.getInt("maximum overtop tracks", "options", 1, 1, 10, "The maximum number of tracks that can play over top of the background music at once.");
		
		backgroundFade = config.getFloat("background fade", "options", 0.4f, 0.0001f, 1.0f, "How much the background music will fade to when something is playing over top of it. Note this is only when 'overtop' is true; otherwise it will always fade to almost nothing.");
		fadeStrength = config.getInt("fade strength", "options", 5, 0, 100, "How fast the background music fades when it changes volume.");
		
		menuTickDelayMin = config.getInt("menu music delay minimum", "options", 20, 0, Integer.MAX_VALUE, "Minimum menu music delay.");
		menuTickDelayMax = config.getInt("menu music delay maximum", "options", 600, 0, Integer.MAX_VALUE, "Maximum menu music delay.");
		
		ingameTickDelayMin = config.getInt("ingame music delay minimum", "options", 1200, 0, Integer.MAX_VALUE, "Minimum in-game music delay.");
		ingameTickDelayMax = config.getInt("ingame music delay maximum", "options", 3600, 0, Integer.MAX_VALUE, "Maximum in-game music delay.");
		
		playVanilla = config.getBoolean("play vanilla", "options", true, "Play vanilla tracks as default if nothing else is found for the current situation.");
		
		stopTracks = config.getBoolean("stop tracks", "options", true, "Stop any background tracks that no longer apply (for instance when you move to a biome where it should no longer play) and attempt to play something new.");
		
		battleDistance = config.getInt("battle max distance", "options", 8, 1, Integer.MAX_VALUE, "The distance an enemy should be away from you for battle music to stop.");
		battleMonsterOnly = config.getBoolean("battle music for only monsters", "options", true, "Only apply blacklisted battle tracks to entities considered monsters.");
		
		config.save();
		
		MChRegisterer.registerEventHandlers();
		MChRegisterer.registerTickHandlers();
		MChRegisterer.registerResourceReloadListeners();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	
    	if(event.getSide() == Side.SERVER) {
    		return; //Don't load on the server
    	}
    	
    	//Replace the vanilla music ticker with our custom one
    	
    	Minecraft mc = Minecraft.getMinecraft();
    	MusicChoicesMusicTicker.ticker = new MusicChoicesMusicTicker(mc);
    	
    	try {
    		for(Field f : mc.getClass().getDeclaredFields()) {
    			if(f.getName().equals("mcMusicTicker") || f.getName().equals("field_147126_aw")) {
    				if(debug) System.out.println("[Music Choices] Found music ticker in Minecraft class.");
    				f.setAccessible(true);
    				f.set(mc, MusicChoicesMusicTicker.ticker);
    			}
    		}
    	}
    	catch(Exception e) {
    		throw new ReportedException(new CrashReport("Music Choices couldn't load in it's music ticker! Things won't work. =( Better let Tmtravlr know.", e));
    	}
        
    }
    
    public static boolean isGamePaused() {
    	Minecraft mc = Minecraft.getMinecraft();
    	return mc.isSingleplayer() && mc.currentScreen != null && mc.currentScreen.doesGuiPauseGame() && !(mc.getIntegratedServer() != null && mc.getIntegratedServer().getPublic());
    }
}
