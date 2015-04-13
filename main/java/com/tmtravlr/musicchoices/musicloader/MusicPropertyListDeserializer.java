package com.tmtravlr.musicchoices.musicloader;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;

import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundList;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

import org.apache.commons.lang3.Validate;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmtravlr.musicchoices.MusicChoicesMod;

/**
 * Deserializer to create a MusicPropertyList from a JSON entry.
 * 
 * @author Rebeca Rey
 * @Date Febuary 2015 
 */
public class MusicPropertyListDeserializer implements JsonDeserializer
{
	public MusicPropertyList deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
	{
		if(MusicChoicesMod.super_duper_debug) System.out.println("[Music Choices] Found an entry!");
		
		JsonElement otherElement;
		JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "entry");
		MusicPropertyList properties = new MusicPropertyList();
		
		
		properties.isOptions = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonObject, "options", false);

		if(properties.isOptions) {
			
			//Load options
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] Found a Music Choices options entry!");
			
			if(jsonObject.has("maximum background tracks")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Found maximum background tracks entry.");
				properties.maxBackground = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "maximum background tracks", -1);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - maximum background tracks is " + properties.maxBackground);
			}
			
			if(jsonObject.has("maximum overtop tracks")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Found maximum overtop tracks entry.");
				properties.maxOvertop = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "maximum overtop tracks", -1);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - maximum overtop tracks is " + properties.maxOvertop);
			}
			
			if(jsonObject.has("background fade")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Found background fade entry.");
				properties.backgroundFade = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonObject, "background fade", -1.0f);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - background fade is " + properties.backgroundFade);
			}
			
			if(jsonObject.has("fade strength")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Found fade strength entry.");
				properties.fadeStrength = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "fade strength", -1);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - fade strength is " + properties.fadeStrength);
			}
			
			if(jsonObject.has("menu music delay minimum")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Found menu music delay minimum entry.");
				properties.menuTickDelayMin = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "menu music delay minimum", -1);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - menu music delay minimum is " + properties.menuTickDelayMin);
			}
			
			if(jsonObject.has("menu music delay maximum")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Found menu music delay maximum entry.");
				properties.menuTickDelayMax = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "menu music delay maximum", -1);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - menu music delay maximum is " + properties.menuTickDelayMax);
			}
			
			if(jsonObject.has("ingame music delay minimum")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Found ingame music delay minimum entry.");
				properties.ingameTickDelayMin = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "ingame music delay minimum", -1);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - ingame music delay minimum is " + properties.ingameTickDelayMin);
			}
			
			if(jsonObject.has("ingame music delay maximum")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Found ingame music delay maximum entry.");
				properties.ingameTickDelayMax = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "ingame music delay maximum", -1);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - ingame music delay maximum is " + properties.ingameTickDelayMax);
			}
			
			if(jsonObject.has("play vanilla")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Found play vanilla entry.");
				properties.doPlayVanilla = true;
				properties.playVanilla = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonObject, "play vanilla", true);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - play vanilla is " + properties.playVanilla);
			}
			
			if(jsonObject.has("stop tracks")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Found stop tracks entry.");
				properties.doStopTracks = true;
				properties.stopTracks = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonObject, "stop tracks", true);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - stop tracks is " + properties.stopTracks);
			}
			
			if(jsonObject.has("battle max distance")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Found battle max distance entry.");
				properties.battleDistance = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "battle max distance", -1);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - battle max distance is " + properties.battleDistance);
			}
			
			if(jsonObject.has("battle music for only monsters")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Found battle music for only monsters entry.");
				properties.doBattleMonsterOnly = true;
				properties.battleMonsterOnly = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonObject, "battle music for only monsters", true);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - battle music for only monsters is " + properties.battleMonsterOnly);
			}
			
			return properties;
		}
		
		//If it reaches here, this must be music, not options.
		
		//Load music

		SoundCategory category = SoundCategory.func_147154_a(JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "category", SoundCategory.MASTER.getCategoryName()));
		Validate.notNull(category, "Invalid category", new Object[0]);

		if(category != SoundCategory.MUSIC) {
			//Don't do anything if this isn't a music entry!
			return null;
		}

		if(MusicChoicesMod.super_duper_debug) System.out.println("[Music Choices] Found a music entry!");

		//Load in the properties

		properties.isMusic = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonObject, "musicchoices", false);

		if(!properties.isMusic) {
			//Don't do anything if this shouldn't be handled by Music Choices!
			return null;
		}

		if(MusicChoicesMod.debug) System.out.println("[Music Choices] Found a Music Choices music entry!");

		if(jsonObject.has("overlap")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as overlapping.");
			properties.overlap = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonObject, "overlap", false);
			if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - overlapping is " + properties.overlap);
		}
		
		if(jsonObject.has("priority")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Has a priority.");
			properties.priority = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, "priority", 1);
			if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - priority is " + properties.priority);
		}

		if(jsonObject.has("menu")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as menu music.");
			properties.menu = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonObject, "menu", false);
			if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - menu is " + properties.menu);
		}

		if(jsonObject.has("credits")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as credits music.");
			properties.credits = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonObject, "credits", false);
			if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - credits is " + properties.credits);
		}

		if(jsonObject.has("boss")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as boss music.");
			otherElement = jsonObject.get("boss");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "boss")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "boss");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String nbtString = JsonUtils.getJsonElementStringValue(otherElement, "boss entry");
					loadNBTEntry(nbtString, properties.bossTags);
				}
			}
		}
		
		if(jsonObject.has("boss stop")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as boss stop music.");
			otherElement = jsonObject.get("boss stop");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "boss stop")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "boss stop");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String nbtString = JsonUtils.getJsonElementStringValue(otherElement, "boss stop entry");
					loadNBTEntry(nbtString, properties.bossStopTags);
				}
			}
		}

		if(jsonObject.has("victory")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as victory music.");
			otherElement = jsonObject.get("victory");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "victory")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "victory");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String nbtString = JsonUtils.getJsonElementStringValue(otherElement, "victory entry");
					loadNBTEntry(nbtString, properties.victoryTags);
				}
			}
		}
		
		if(jsonObject.has("battle")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as battle music.");
			otherElement = jsonObject.get("battle");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "battle")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "battle");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String entity = JsonUtils.getJsonElementStringValue(otherElement, "battle entry");
					properties.battleEntities.add(entity);
				}
			}
		}
		
		if(jsonObject.has("battle blacklist")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as battle blacklist music.");
			if(properties.battleBlacklistEntities == null) {
				properties.battleBlacklistEntities = new HashSet<String>();
			}
			otherElement = jsonObject.get("battle blacklist");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "battle blacklist")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "battle blacklist");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String entity = JsonUtils.getJsonElementStringValue(otherElement, "battle blacklist entry");
					properties.battleBlacklistEntities.add(entity);
				}
			}
		}
		
		if(jsonObject.has("battle stop")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as battle stop music.");
			otherElement = jsonObject.get("battle stop");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "battle stop")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "battle stop");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String entity = JsonUtils.getJsonElementStringValue(otherElement, "battle stop entry");
					properties.battleStopEntities.add(entity);
				}
			}
		}

		if(jsonObject.has("achievements")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as achievement music.");

			otherElement = jsonObject.get("achievements");
			if(JsonUtils.jsonElementTypeIsString(otherElement)) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - It's a string.");
				String value = JsonUtils.getJsonElementStringValue(otherElement, "achievements");
				if(value.equals("all")) {
					properties.allAchievements = true;
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]         - Marked as 'all'");
				}
				else {
					properties.achievements.add(value);
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]         - achievement is " + value);
				}
			}
			else if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "achievements")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - It's an array of strings.");
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "achievements");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getJsonElementStringValue(otherElement, "achievements");
					properties.achievements.add(value);
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]         - achievement is " + value);
				}
			}
		}

		if(jsonObject.has("event")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as event music.");
			properties.event = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "event");
			if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - event is " + properties.event);
		}

		if(jsonObject.has("creative")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as creative/not creative music.");
			if(JsonUtils.jsonElementTypeIsString(jsonElement)) {
				String value = JsonUtils.getJsonElementStringValue(jsonElement, "creative");
				if(value.equalsIgnoreCase("true")) {
					properties.allGamemodes = false;
					properties.creative = true;
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - creative is " + properties.creative);
				}
				else if(value.equalsIgnoreCase("false")) {
					properties.allGamemodes = false;
					properties.creative = false;
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - creative is " + properties.creative);
				}
				else {
					properties.allGamemodes = true;
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - creative is all gamemodes.");
				}
			}
			else {
				properties.allGamemodes = false;
				properties.creative = JsonUtils.getJsonObjectBooleanFieldValueOrDefault(jsonObject, "creative", false);
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - creative is " + properties.creative);
			}
		}

		if(jsonObject.has("biomes")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as biome music.");
			otherElement = jsonObject.get("biomes");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "biomes")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "biomes");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getJsonElementStringValue(otherElement, "biomes");
					if(properties.biomes == null) {
						properties.biomes = new HashSet<String>();
					}
					properties.biomes.add(value);
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - biome is " + value);
				}
			}
		}
		
		if(jsonObject.has("biome blacklist")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as biome blacklist music.");
			otherElement = jsonObject.get("biome blacklist");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "biome blacklist")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "biome blacklist");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getJsonElementStringValue(otherElement, "biome blacklist entry");
					properties.biomeBlacklist.add(value);
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - biome is " + value);
				}
			}
		}

		if(jsonObject.has("biome types")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as biome type music.");
			otherElement = jsonObject.get("biome types");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "biome types")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "biome types");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getJsonElementStringValue(otherElement, "biome types");
					if(properties.biomeTypes == null) {
						properties.biomeTypes = new HashSet<String>();
					}
					properties.biomeTypes.add(value);
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - biome type is " + value);
				}
			}
		}
		
		if(jsonObject.has("biome type blacklist")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as biome type blacklist music.");
			otherElement = jsonObject.get("biome type blacklist");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "biome type blacklist")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "biome type blacklist");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getJsonElementStringValue(otherElement, "biome type blacklist entry");
					properties.biomeTypeBlacklist.add(value);
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - biome type is " + value);
				}
			}
		}

		if(jsonObject.has("dimensions")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as dimension music.");
			otherElement = jsonObject.get("dimensions");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "dimensions")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "dimensions");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					int value = JsonUtils.getJsonElementIntegerValue(otherElement, "dimensions entry");
					if(properties.dimensions == null) {
						properties.dimensions = new HashSet<Integer>();
					}
					properties.dimensions.add(value);
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - dimension is " + value);
				}
			}
		}

		if(jsonObject.has("dimension blacklist")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as dimension blacklist music.");
			otherElement = jsonObject.get("dimension blacklist");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "dimension blacklist")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "dimension blacklist");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					int value = JsonUtils.getJsonElementIntegerValue(otherElement, "dimension blacklist entry");
					properties.dimensionBlacklist.add(value);
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - dimension is " + value);
				}
			}
		}

		if(jsonObject.has("lighting")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as lighting music.");
			otherElement = jsonObject.get("lighting");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "lighting")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "lighting");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getJsonElementStringValue(otherElement, "lighting");
					if(properties.lighting == null) {
						properties.lighting = new HashSet<String>();
					}
					properties.lighting.add(value);
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - lighting is " + value);
				}
			}
		}
		
		if(jsonObject.has("time")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as time music.");
			otherElement = jsonObject.get("time");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "time")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "time");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getJsonElementStringValue(otherElement, "time");
					if(properties.time == null) {
						properties.time = new HashSet<String>();
					}
					properties.time.add(value);
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - time is " + value);
				}
			}
		}
		
		if(jsonObject.has("weather")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Marked as weather music.");
			otherElement = jsonObject.get("weather");
			if(JsonUtils.jsonObjectFieldTypeIsArray(jsonObject, "weather")) {
				JsonArray jsonarray = JsonUtils.getJsonObjectJsonArrayField(jsonObject, "weather");

				for (int i = 0; i < jsonarray.size(); ++i) {
					otherElement = jsonarray.get(i);
					String value = JsonUtils.getJsonElementStringValue(otherElement, "weather");
					if(properties.weather == null) {
						properties.weather = new HashSet<String>();
					}
					properties.weather.add(value);
					if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - weather is " + value);
				}
			}
		}
		
		if(jsonObject.has("height minimum")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Height minimum specified.");
			properties.heightMin = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "height minimum");
			if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - height minimum is " + properties.heightMin);
		}
		
		if(jsonObject.has("height maximum")) {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices] - Height maximum specified.");
			properties.heightMax = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "height maximum");
			if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - height maximum is " + properties.heightMax);
		}

		//Done!

		return properties;
	}
	
	private void loadNBTEntry(String nbtString, HashSet<NBTTagCompound> tagSet) {
		NBTTagCompound tag = null;
		try {
            NBTBase nbtbase = JsonToNBT.func_150315_a(nbtString);

            if (nbtbase instanceof NBTTagCompound) {
            	tag = (NBTTagCompound)nbtbase;
            }
        }
        catch (NBTException nbtexception)
        {
        	System.out.println("[Music Choices]     - Problem while loading NBT tag!");
        	nbtexception.printStackTrace();
        }
		if(tag != null) {
			
			tagSet.add(tag);
			
			if(tag.hasKey("id")) {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - entity is " + tag.getString("id"));
			}
			else {
				if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - entity tag is " + nbtString);
			}
		}
		else {
			if(MusicChoicesMod.debug) System.out.println("[Music Choices]     - didn't recognize entity tag. =(");
		}
	}
}
