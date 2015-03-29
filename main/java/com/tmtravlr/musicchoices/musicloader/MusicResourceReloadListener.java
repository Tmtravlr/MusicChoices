package com.tmtravlr.musicchoices.musicloader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tmtravlr.musicchoices.MusicChoicesMod;
import com.tmtravlr.musicchoices.MusicProperties;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundList;
import net.minecraft.client.audio.SoundListSerializer;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/**
 * Detects when the resource packs reload, and loads in the custom entries from the sounds.json file.
 * 
 * @author Rebeca Rey
 * @Date Febuary 2015 
 */
public class MusicResourceReloadListener implements IResourceManagerReloadListener {

	private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(MusicPropertyList.class, new MusicPropertyListDeserializer()).create();
    private static final ParameterizedType TYPE = new ParameterizedType()
    {
        public Type[] getActualTypeArguments()
        {
            return new Type[] {String.class, MusicPropertyList.class};
        }
        public Type getRawType()
        {
            return Map.class;
        }
        public Type getOwnerType()
        {
            return null;
        }
    };
	
	@Override
	public void onResourceManagerReload(IResourceManager manager) {
		if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loading resources...");
		
		//Clear out all the previous music, if any.
		MusicProperties.clearAllLists();
		
		Iterator iterator = manager.getResourceDomains().iterator();

        while (iterator.hasNext())
        {
            String s = (String)iterator.next();

            try
            {
                List list = manager.getAllResources(new ResourceLocation(s, "sounds.json"));
                Iterator iterator1 = list.iterator();

                while (iterator1.hasNext())
                {
                    IResource iresource = (IResource)iterator1.next();

                    try
                    {
                    	if (MusicChoicesMod.super_duper_debug) System.out.println("[Music Choices] Loading in a Sounds.json file.");
                    	
                        Map map = (Map)GSON.fromJson(new InputStreamReader(iresource.getInputStream()), TYPE);
                        Iterator iterator2 = map.entrySet().iterator();
                        while (iterator2.hasNext())
                        {
                            Entry entry = (Entry)iterator2.next();
                            
                            //Don't load in music that isn't meant to be loaded in.
                        	if (entry.getValue() != null) { 
                        		if (((MusicPropertyList)entry.getValue()).isMusic) {
	                        		if(MusicChoicesMod.super_duper_debug) System.out.println("[Music Choices] Loading properties for " + s + ":" + (String)entry.getKey());
	                        		this.loadMusicProperties(s, (String)entry.getKey(), (MusicPropertyList)entry.getValue());
	                        	}
                        		else if (((MusicPropertyList)entry.getValue()).isOptions && !MusicChoicesMod.overrideJsonOptions) {
                        			this.loadMusicOptions((MusicPropertyList)entry.getValue());
                        		}
                        	}
                        }
                    }
                    catch (RuntimeException runtimeexception)
                    {
                    	runtimeexception.printStackTrace();
                        //logger.warn("Invalid sounds.json", runtimeexception);
                    }
                }
            }
            catch (IOException ioexception)
            {
                ;
            }
        }
	}
	
	private void loadMusicOptions(MusicPropertyList propertyList) {
		if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loading options.");
		
		if(propertyList.maxBackground > 0) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loaded max background as " + propertyList.maxBackground);
			MusicChoicesMod.maxBackground = propertyList.maxBackground;
		}
		
		if(propertyList.maxOvertop > 0) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loaded max overtop as " + propertyList.maxOvertop);
			MusicChoicesMod.maxOvertop = propertyList.maxOvertop;
		}
		
		if(propertyList.backgroundFade > 0.0001f) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loaded background fade as " + propertyList.backgroundFade);
			MusicChoicesMod.backgroundFade = propertyList.backgroundFade;
		}
		
		if(propertyList.fadeStrength > 0) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loaded fade strength as " + propertyList.fadeStrength);
			MusicChoicesMod.fadeStrength = propertyList.fadeStrength;
		}
		
		if(propertyList.menuTickDelayMin >= 0) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loaded min menu tick as " + propertyList.menuTickDelayMin);
			MusicChoicesMod.menuTickDelayMin = propertyList.menuTickDelayMin;
		}
		
		if(propertyList.menuTickDelayMax >= 0) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loaded max menu tick as " + propertyList.menuTickDelayMax);
			MusicChoicesMod.menuTickDelayMax = propertyList.menuTickDelayMax;
		}
		
		if(propertyList.ingameTickDelayMin >= 0) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loaded min ingame tick as " + propertyList.ingameTickDelayMin);
			MusicChoicesMod.ingameTickDelayMin = propertyList.ingameTickDelayMin;
		}
		
		if(propertyList.ingameTickDelayMax >= 0) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loaded max ingame tick as " + propertyList.ingameTickDelayMax);
			MusicChoicesMod.ingameTickDelayMax = propertyList.ingameTickDelayMax;
		}
		
		if(propertyList.doPlayVanilla) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loaded play vanilla as " + propertyList.playVanilla);
			MusicChoicesMod.playVanilla = propertyList.playVanilla;
		}
		
		if(propertyList.doStopTracks) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loaded stop tracks as " + propertyList.stopTracks);
			MusicChoicesMod.stopTracks = propertyList.stopTracks;
		}
		
		if(propertyList.battleDistance >= 0) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loaded battle distance as " + propertyList.battleDistance);
			MusicChoicesMod.battleDistance = propertyList.battleDistance;
		}
		
		if(propertyList.doBattleMonsterOnly) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Loaded battle monster only as " + propertyList.battleMonsterOnly);
			MusicChoicesMod.battleMonsterOnly = propertyList.battleMonsterOnly;
		}
	}
	
	private void loadMusicProperties(String domain, String name, MusicPropertyList propertyList) {
		MusicProperties entry = new MusicProperties(new ResourceLocation(domain, name), propertyList);
		
		if(propertyList.menu) {
			MusicProperties.menuList.add(entry);
			return;
		}
		
		if(propertyList.credits) {
			MusicProperties.creditsList.add(entry);
			return;
		}
		
		//Do boss and victory entries
		
		if(!propertyList.bossTags.isEmpty()) {
			for(NBTTagCompound tag : propertyList.bossTags) {
				//if(MusicChoicesMod.debug) System.out.println("Adding boss" + (tag.getString("id").equals("") ? "." : " called " + tag.getString("id")));
				ArrayList<MusicProperties> bossEntry = MusicProperties.bossMap.get(tag);
				if(bossEntry == null) {
					bossEntry = new ArrayList<MusicProperties>();
				}
				bossEntry.add(entry);
				MusicProperties.bossMap.put(tag, bossEntry);
				
			}
			return;
		}
		
		if(!propertyList.bossStopTags.isEmpty()) {
			for(NBTTagCompound tag : propertyList.bossStopTags) {
				ArrayList<MusicProperties> bossEntry = MusicProperties.bossStopMap.get(tag);
				if(bossEntry == null) {
					bossEntry = new ArrayList<MusicProperties>();
				}
				bossEntry.add(entry);
				MusicProperties.bossStopMap.put(tag, bossEntry);
				
			}
			return;
		}
		
		if(!propertyList.victoryTags.isEmpty()) {
			for(NBTTagCompound tag : propertyList.victoryTags) {
				ArrayList<MusicProperties> victoryEntry = MusicProperties.victoryMap.get(tag);
				if(victoryEntry == null) {
					victoryEntry = new ArrayList<MusicProperties>();
				}
				victoryEntry.add(entry);
				MusicProperties.victoryMap.put(tag, victoryEntry);
			}
			return;
		}
		
		//Do battle entries
		
		if(!propertyList.battleEntities.isEmpty()) {
			for(String entityName : propertyList.battleEntities) {
				ArrayList<MusicProperties> musicList = MusicProperties.battleMap.get(entityName);
				if(musicList == null) {
					musicList = new ArrayList<MusicProperties>();
				}
				musicList.add(entry);
				MusicProperties.battleMap.put(entityName, musicList);
			}
			return;
		}
		
		if(propertyList.battleBlacklistEntities != null && !propertyList.battleBlacklistEntities.isEmpty()) {
			MusicProperties.battleBlacklisted.add(entry);
			return;
		}
		
		if(!propertyList.battleStopEntities.isEmpty()) {
			for(String entityName : propertyList.battleStopEntities) {
				ArrayList<MusicProperties> musicList = MusicProperties.battleStopMap.get(entityName);
				if(musicList == null) {
					musicList = new ArrayList<MusicProperties>();
				}
				musicList.add(entry);
				MusicProperties.battleStopMap.put(entityName, musicList);
			}
			return;
		}
		
		//Do event entries
		
		if(propertyList.event != null) {
			if(propertyList.event.equalsIgnoreCase("login")) {
				MusicProperties.loginList.add(entry);
				return;
			}
			
			if(propertyList.event.equalsIgnoreCase("death")) {
				MusicProperties.deathList.add(entry);
				return;
			}
			
			if(propertyList.event.equalsIgnoreCase("respawn")) {
				MusicProperties.respawnList.add(entry);
				return;
			}
			
			if(propertyList.event.equalsIgnoreCase("sunrise")) {
				MusicProperties.sunriseList.add(entry);
				return;
			}
			
			if(propertyList.event.equalsIgnoreCase("sunset")) {
				MusicProperties.sunsetList.add(entry);
				return;
			}
		}
		
		if(propertyList.allAchievements) {
			ArrayList<MusicProperties> achList = MusicProperties.achievementMap.get("all");
			if(achList == null) {
				achList = new ArrayList<MusicProperties>();
			}
			achList.add(entry);
			
			MusicProperties.achievementMap.put("all", achList);
			return;
		}
		
		if(!propertyList.achievements.isEmpty()) {
			for(String ach : propertyList.achievements) {
				ArrayList<MusicProperties> achList = MusicProperties.achievementMap.get(ach);
				if(achList == null) {
					achList = new ArrayList<MusicProperties>();
				}
				achList.add(entry);
				
				MusicProperties.achievementMap.put(ach, achList);
				return;
			}
		}
		
		//If all else fails, this must be regular in-game music.
		MusicProperties.ingameList.add(entry);
	}

}
