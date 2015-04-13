package com.tmtravlr.musicchoices;

import java.util.*;

import com.tmtravlr.musicchoices.MChHelper.BackgroundMusic;
import com.tmtravlr.musicchoices.musicloader.MusicPropertyList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager.BiomeType;

/**
 * Holds info about each property for the rest of the mod to access.
 * Also holds some static methods for selecting music from it's lists.
 * 
 * @author Rebeca Rey
 * @Date Febuary 2015 
 */
public class MusicProperties {
	
	private static Random rand = new Random();
	private static Minecraft mc = Minecraft.getMinecraft();
	
	//Some quick access static lists
	
	public static ArrayList<MusicProperties> menuList = new ArrayList<MusicProperties>();
	
	public static ArrayList<MusicProperties> creditsList = new ArrayList<MusicProperties>();
	
	public static ArrayList<MusicProperties> loginList = new ArrayList<MusicProperties>();
	
	public static ArrayList<MusicProperties> deathList = new ArrayList<MusicProperties>();
	
	public static ArrayList<MusicProperties> respawnList = new ArrayList<MusicProperties>();
	
	public static ArrayList<MusicProperties> sunriseList = new ArrayList<MusicProperties>();
	
	public static ArrayList<MusicProperties> sunsetList = new ArrayList<MusicProperties>();
	
	public static HashMap<String, ArrayList<MusicProperties>> achievementMap = new HashMap<String, ArrayList<MusicProperties>>();
	
	public static HashMap<NBTTagCompound, ArrayList<MusicProperties>> bossMap = new HashMap<NBTTagCompound, ArrayList<MusicProperties>>();
	
	public static HashMap<NBTTagCompound, ArrayList<MusicProperties>> bossStopMap = new HashMap<NBTTagCompound, ArrayList<MusicProperties>>();
	
	public static HashMap<NBTTagCompound, ArrayList<MusicProperties>> victoryMap = new HashMap<NBTTagCompound, ArrayList<MusicProperties>>();
	
	public static HashMap<String, ArrayList<MusicProperties>> battleMap = new HashMap<String, ArrayList<MusicProperties>>();
	
	public static ArrayList<MusicProperties> battleBlacklisted = new ArrayList<MusicProperties>();
	
	public static HashMap<String, ArrayList<MusicProperties>> battleStopMap = new HashMap<String, ArrayList<MusicProperties>>();
	
	public static ArrayList<MusicProperties> ingameList = new ArrayList<MusicProperties>();
	
	
	//Static methods to do useful things.
	
	public static void clearAllLists() {
		menuList.clear();
		creditsList.clear();
		loginList.clear();
		deathList.clear();
		respawnList.clear();
		sunriseList.clear();
		sunsetList.clear();
		achievementMap.clear();
		bossMap.clear();
		bossStopMap.clear();
		victoryMap.clear();
		battleMap.clear();
		battleBlacklisted.clear();
		battleStopMap.clear();
		ingameList.clear();
	}
	
	//Find a music track that should be playing based on the state the game is currently in
	public static MusicProperties findTrackForCurrentSituation() {
		
		if (!menuList.isEmpty() && (mc.currentScreen instanceof GuiMainMenu || mc.thePlayer == null)) {
			return menuList.get(rand.nextInt(menuList.size()));
		}
		
		if (!ingameList.isEmpty() && mc.theWorld != null && mc.thePlayer != null) {
			return findTrackForCurrentSituationFromList(ingameList);
		}
		
		return null;
	}
	
	//Attempts to find a music track from the given map that applies to the given entity.
	public static MusicProperties findMusicFromNBTMap(EntityLivingBase entity, HashMap<NBTTagCompound, ArrayList<MusicProperties>> nbtMap) {
		ArrayList<ArrayList<MusicProperties>> applicableLists = new ArrayList<ArrayList<MusicProperties>>();
		
		for(NBTTagCompound currentTag : nbtMap.keySet()) {
			ArrayList<MusicProperties> currentList = nbtMap.get(currentTag);
			
			if(currentList != null && !currentList.isEmpty()) {
				NBTTagCompound entityTag = new NBTTagCompound();
				entity.writeToNBT(entityTag);
				if(EntityList.getEntityString(entity) != null && !EntityList.getEntityString(entity).equals("")) {
					entityTag.setString("id", EntityList.getEntityString(entity));
				}
				
				if(MusicChoicesMod.super_duper_debug) System.out.println("[Music Choices] Entity tag: " + entityTag);
				
				//Check that the entity has all tags
				if(hasAllTags(currentTag, entityTag)) {
					applicableLists.add(currentList);
				}
			}
		}
		
		if(!applicableLists.isEmpty()) {
			ArrayList<MusicProperties> returnList = applicableLists.get(rand.nextInt(applicableLists.size()));
			return findTrackForCurrentSituationFromList(returnList);
		}
		
		return null;
	}
	
	public static MusicProperties findMusicFromStringMap(String string, HashMap<String, ArrayList<MusicProperties>> stringMap) {
		ArrayList<MusicProperties> musicList = stringMap.get(string);
		
		if(musicList != null) {
			return findTrackForCurrentSituationFromList(musicList);
		}
		
		return null;
	}
	
	//Check that the target nbt tag has all the ones it should have
	public static boolean hasAllTags(NBTTagCompound tagToHave, NBTTagCompound target) {
		
		Set<String> tagMap = tagToHave.func_150296_c();
		for(String tag : tagMap) {
//			if(tagToHave.func_150299_b(tag) == 9 && target.func_150299_b(tag) == 9) {
//				NBTTagList targetList = (NBTTagList)target.getTag(tag);
//				NBTTagList compareList = (NBTTagList)tagToHave.getTag(tag);
//				
//				if(targetList.func_150303_d() == compareList.func_150303_d()) {
//					if(targetList.func_150303_d() == 2)
//					if(targetList.func_150303_d() == 10) {
//						
//					}
//					else {
//						
//					}
//				}
//			}
			if(tagToHave.func_150299_b(tag) == 10 && target.func_150299_b(tag) == 10) {
				return hasAllTags(tagToHave.getCompoundTag(tag), target.getCompoundTag(tag));
			}
			if(!tagToHave.getTag(tag).equals(target.getTag(tag))) {
				return false;
			}
		}
			
		return true;
	}
	
	//Find a music track that should be playing in the player's current situation from the given list
	public static MusicProperties findTrackForCurrentSituationFromList(ArrayList<MusicProperties> propertyList) {
		
		int maxPriority = 1;
		ArrayList<MusicProperties> releventList = new ArrayList<MusicProperties>();
		
		for(MusicProperties music : propertyList) {
			
			if(checkIfPropertiesApply(music.propertyList)) {
				//If it all checks out, add it to the list
				
				if(music.propertyList.priority > maxPriority) {
					//We have a higher-priority track. Clear out all others.
					releventList.clear();
					maxPriority = music.propertyList.priority;
				}
				
				if(music.propertyList.priority == maxPriority) {
					releventList.add(music);
				}
			}
			
		}
		
		if(!releventList.isEmpty()) {
			return releventList.get(rand.nextInt(releventList.size()));
		}
		
		return null;
	}
	
	public static MusicProperties findBattleMusicFromBlacklist(String entityName) {
		ArrayList<MusicProperties> releventList = new ArrayList<MusicProperties>();
		
		for(MusicProperties music : battleBlacklisted) {
			if(music.propertyList.battleBlacklistEntities != null && !music.propertyList.battleBlacklistEntities.contains(entityName)) {
				releventList.add(music);
			}
		}
		
		if(!releventList.isEmpty()) {
			return findTrackForCurrentSituationFromList(releventList);
		}
		
		return null;
	}
	
	public static MusicProperties findTrackForAchievement(String achName) {
		if(!achievementMap.containsKey(achName)) {
			achName = "all";
		}
		
		return findMusicFromStringMap(achName, achievementMap);
	}
	
	public static boolean checkIfMusicStillApplies(BackgroundMusic music, MusicTicker.MusicType vanillaMusicType) {
		
		//If properties are null, assume this is a vanilla track.
		if(music.properties == null) {
			return vanillaMusicType.getMusicTickerLocation().equals(music.music.getPositionedSoundLocation());
		}
		
		//First check for menu or credits music
		
		if(music.properties.menu) {
			return mc.currentScreen instanceof GuiMainMenu || mc.thePlayer == null;
		}
		
		if(music.properties.credits) {
			return mc.currentScreen instanceof GuiWinGame;
		}
		
		//Then check for music that should play in-game
		
		if(mc.theWorld != null && mc.thePlayer != null) {
			if(!checkIfPropertiesApply(music.properties)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean checkIfPropertiesApply(MusicPropertyList properties) {
		int dimension = mc.theWorld.provider.dimensionId;
		int x = MathHelper.floor_double(mc.thePlayer.posX);
		int y = MathHelper.floor_double(mc.thePlayer.posY);
		int z = MathHelper.floor_double(mc.thePlayer.posZ);
		BiomeGenBase biome = mc.theWorld.getBiomeGenForCoords(x, z);
		boolean isCreative = mc.thePlayer.capabilities.isCreativeMode;
		Chunk chunk = mc.theWorld.getChunkFromBlockCoords(x, z);
		//Note for the two below: if below the world, assume it's "underground", and if above the world, assume it's open sky
		boolean isArtificialLight = (y >= 0 && y < 256) ? chunk.getSavedLightValue(EnumSkyBlock.Block, x & 15, y, z & 15) >= 7 : false;
		boolean isSky = (y >= 0 && y < 256) ? chunk.getSavedLightValue(EnumSkyBlock.Sky, x & 15, y, z & 15) >= 7 : y < 0 ? false : true;
		boolean isDay = mc.theWorld.getSunBrightness(1.0F) > 0.5F;
		boolean isRain = mc.theWorld.isRaining() && !mc.theWorld.isThundering();
		boolean isStorm = mc.theWorld.isThundering();
		boolean isClear = !(isRain || isStorm);
		
		//Check if the player is in the right gamemode
		if(!properties.allGamemodes) {
			//If they are different
			if(isCreative != properties.creative) {
				return false;
			}
		}
		
		//Make sure this biome is allowed
		if(properties.biomes != null && !properties.biomes.contains(biome.biomeName)) {
			return false;
		}
		
		if(properties.biomeBlacklist != null && properties.biomeBlacklist.contains(biome.biomeName)) {
			return false;
		}
		
		//Make sure at least one of this biome's types are allowed
		if(properties.biomeTypes != null) {
			boolean hasType = false;
			
			for (BiomeDictionary.Type type : BiomeDictionary.getTypesForBiome(biome)) {
				if(properties.biomeTypes.contains(type.name())) {
					hasType = true;
					break;
				}
			}
			
			if(!hasType) {
				return false;
			}
		}
		
		if(properties.biomeTypeBlacklist != null) {
			for (BiomeDictionary.Type type : BiomeDictionary.getTypesForBiome(biome)) {
				if(properties.biomeTypeBlacklist.contains(type.name())) {
					return false;
				}
			}
		}
		
		//Make sure this dimension is allowed
		if(properties.dimensions != null && !properties.dimensions.contains(dimension)) {
			return false;
		}
		
		if(properties.dimensionBlacklist != null && properties.dimensionBlacklist.contains(dimension)) {
			return false;
		}
		
		//Check the lighting
		if(properties.lighting != null) {
			if(isSky && isDay && !properties.lighting.contains("sun")) {
				return false;
			}
			
			if(isSky && !isDay && !properties.lighting.contains("moon")) {
				return false;
			}
			
			if(!isSky && isArtificialLight && !properties.lighting.contains("light")) {
				return false;
			}
			
			if(!isSky && !isArtificialLight && !properties.lighting.contains("dark")) {
				return false;
			}
		}
		
		//Check the time
		if(properties.time != null) {
			if(isDay && !properties.time.contains("day")) {
				return false;
			}
			
			if(!isDay && !properties.time.contains("night")) {
				return false;
			}
		}
		
		if(properties.weather != null) {
			if(isRain && !properties.weather.contains("rain")) {
				return false;
			}
			
			if(isStorm && !properties.weather.contains("storm")) {
				return false;
			}
			
			if(isClear && !properties.weather.contains("clear")) {
				return false;
			}
		}
		
		//Check the height
		
		if(y < properties.heightMin) {
			return false;
		}
		
		if(y > properties.heightMax) {
			return false;
		}
		
		return true;
	}
	
	
	
	
	//Actual music properties class start:
	
	//The sound
	public ResourceLocation location = null;
	
	//The properties of the music track(s) loaded in.
	public MusicPropertyList propertyList;
	
	public MusicProperties(ResourceLocation locationToSet, MusicPropertyList listToSet) {
		location = locationToSet;
		propertyList = listToSet;
	}
	
}
