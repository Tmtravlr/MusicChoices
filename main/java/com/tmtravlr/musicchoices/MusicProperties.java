package com.tmtravlr.musicchoices;

import java.util.*;

import com.tmtravlr.musicchoices.MusicChoicesMusicTicker.BackgroundMusic;
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
	
	public static HashMap<NBTTagCompound, ArrayList<MusicProperties>> victoryMap = new HashMap<NBTTagCompound, ArrayList<MusicProperties>>();
	
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
		ingameList.clear();
		achievementMap.clear();
		bossMap.clear();
		victoryMap.clear();
	}
	
	//Find a music track that should be playing based on the state the game is currently in
	public static MusicProperties findTrackForCurrentSituation() {
		
		if (!menuList.isEmpty() && (mc.currentScreen instanceof GuiMainMenu || mc.thePlayer == null)) {
			return menuList.get(rand.nextInt(menuList.size()));
		}
		
		if (!creditsList.isEmpty() && mc.currentScreen instanceof GuiWinGame) {
			return creditsList.get(rand.nextInt(menuList.size()));
		}
		
		if (!ingameList.isEmpty()) {
			return findTrackForCurrentSituationFromList(ingameList);
		}
		
		return null;
	}
	
	//Find music to play for this entity, if it's a boss
	public static MusicProperties findBossBattleMusic(EntityLivingBase entity) {
		ArrayList<ArrayList<MusicProperties>> applicableLists = new ArrayList<ArrayList<MusicProperties>>();
		
		for(NBTTagCompound bossTag : bossMap.keySet()) {
			ArrayList<MusicProperties> bossList = bossMap.get(bossTag);
			
			if(bossList != null && !bossList.isEmpty()) {
				NBTTagCompound entityTag = new NBTTagCompound();
				entity.writeToNBT(entityTag);
				entityTag.setString("id", EntityList.getEntityString(entity));
				if(MusicChoicesMod.super_duper_debug) System.out.println("[Music Choices] Entity tag: " + entityTag);
				
				//Check that the entity has all tags
				if(hasAllTags(bossTag, entityTag)) {
					applicableLists.add(bossList);
				}
			}
		}
		
		if(!applicableLists.isEmpty()) {
			ArrayList<MusicProperties> bossList = applicableLists.get(rand.nextInt(applicableLists.size()));
			return findTrackForCurrentSituationFromList(bossList);
		}
		
		return null;
	}
	
	//Find victory music to play when this entity is killed.
	public static MusicProperties findBossVictoryMusic(EntityLivingBase entity) {
		ArrayList<ArrayList<MusicProperties>> applicableLists = new ArrayList<ArrayList<MusicProperties>>();
		
		for(NBTTagCompound victoryTag : victoryMap.keySet()) {
			ArrayList<MusicProperties> victoryList = victoryMap.get(victoryTag);
			
			if(victoryList != null && !victoryList.isEmpty()) {
				NBTTagCompound entityTag = new NBTTagCompound();
				entity.writeToNBT(entityTag);
				entityTag.setString("id", EntityList.getEntityString(entity));
				
				//Check that the entity has all tags
				if(hasAllTags(victoryTag, entityTag)) {
					applicableLists.add(victoryList);
				}
			}
		}
		
		if(!applicableLists.isEmpty()) {
			ArrayList<MusicProperties> victoryList = applicableLists.get(rand.nextInt(applicableLists.size()));
			return findTrackForCurrentSituationFromList(victoryList);
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
		
		ArrayList<MusicProperties> releventList = new ArrayList<MusicProperties>();
		
		for(MusicProperties music : propertyList) {
			
			if(checkIfPropertiesApply(music.propertyList)) {
				//If it all checks out, add it to the list
				releventList.add(music);
			}
			
		}
		
		if(!releventList.isEmpty()) {
			return releventList.get(rand.nextInt(releventList.size()));
		}
		
		return null;
	}
	
	public static MusicProperties findTrackForAchievement(String achName) {
		if(!achievementMap.containsKey(achName)) {
			achName = "all";
		}
		
		ArrayList<MusicProperties> achList = achievementMap.get(achName);
		
		if(achList == null) {
			achList = new ArrayList<MusicProperties>();
		}
		
		if(!achList.isEmpty()) {
			return findTrackForCurrentSituationFromList(achList);
		}
			
		return null;
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
		boolean isArtificialLight = chunk.getSavedLightValue(EnumSkyBlock.Block, x & 15, y, z & 15) >= 7;
		boolean isSky = chunk.getSavedLightValue(EnumSkyBlock.Sky, x & 15, y, z & 15) >= 7;
		boolean isDay = mc.theWorld.getSunBrightness(1.0F) > 0.751F;
		boolean isRain = mc.theWorld.isRaining() && !mc.theWorld.isThundering();
		boolean isStorm = mc.theWorld.isThundering();
		boolean isClear = !(isRain || isStorm);
		
		//Check if the player is in the right gamemode
		if(!properties.allGamemodes) {
			//If they are different
			if(isCreative != !properties.creative) {
				return false;
			}
		}
		
		//Make sure this biome is allowed
		if(properties.biomes != null && !properties.biomes.contains(biome.biomeName)) {
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
		
		//Make sure this dimension is allowed
		if(properties.dimensions != null && !properties.dimensions.contains(dimension)) {
			return false;
		}
		
		if(properties.dimensionBlacklist != null && properties.dimensionBlacklist.contains(dimension)) {
			return false;
		}
		
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
		
		//Blocks
		
		if(!properties.blocks.isEmpty()) {
			for(String blockName : properties.blocks) {
				if(!checkForBlockInBoundingBox(blockName, 7, x, y ,z)) {
					return false;
				}
			}
		}
		
		//Entities
		
		if(!properties.entities.isEmpty()) {
			List<Entity> entityList = mc.theWorld.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(x-7, y-7, z-7, x+7, y+7, z+7));
			ArrayList<String> allEntityNames = new ArrayList<String>();

			for(Entity entity : entityList) {
				allEntityNames.add(entity.getCommandSenderName());
			}
			
			for(String entityName : properties.entities) {
				if(!allEntityNames.contains(entityName)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static boolean checkForBlockInBoundingBox(String blockName, int radius, int posX, int posY, int posZ) {
		
		Object blockObj = Block.blockRegistry.getObject(blockName);
		
		if(blockObj == null || !(blockObj instanceof Block)) {
			return false;
		}
		
		Block block = (Block) blockObj;
		
		for(int x = -radius; x <= radius; x++) {
			for(int y = -radius; y <= radius; y++) {
				for(int z = -radius; z <= radius; z++) {
					if(mc.theWorld.getBlock(posX + x, posY + y, posZ + z) == block) {
						return true;
					}
				}
			}
		}
		
		return false;
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
