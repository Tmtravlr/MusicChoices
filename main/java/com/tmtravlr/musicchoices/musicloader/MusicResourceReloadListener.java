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
                    	if(MusicChoicesMod.super_duper_debug) System.out.println("[Music Choices] Loading in a Sounds.json file.");
                    	
                        Map map = (Map)GSON.fromJson(new InputStreamReader(iresource.getInputStream()), TYPE);
                        Iterator iterator2 = map.entrySet().iterator();
                        while (iterator2.hasNext())
                        {
                            Entry entry = (Entry)iterator2.next();
                            
                            //Don't load in music that isn't meant to be loaded in.
                        	if(entry.getValue() != null && ((MusicPropertyList)entry.getValue()).valid) {
                        		if(MusicChoicesMod.super_duper_debug) System.out.println("[Music Choices] Loading properties for " + s + ":" + (String)entry.getKey());
                        		this.loadMusicProperties(s, (String)entry.getKey(), (MusicPropertyList)entry.getValue());
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
