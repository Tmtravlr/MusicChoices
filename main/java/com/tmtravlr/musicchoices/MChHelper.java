package com.tmtravlr.musicchoices;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;

import com.tmtravlr.musicchoices.musicloader.MusicPropertyList;

/**
 * Contains a few static helper methods.
 * 
 * @author Rebeca Rey
 * @Date March 2015
 */
public class MChHelper {
	
	private static Minecraft mc = Minecraft.getMinecraft(); 
	
	//Classes to hold info about the background and overtop music
	
	public static class BackgroundMusic {
		public MusicTickable music;
		public MusicPropertyList properties;
		
		public BackgroundMusic(MusicTickable musicToSet, MusicPropertyList propToSet) {
			music = musicToSet;
			properties = propToSet;
		}
	}
	
	public static class OvertopMusic {
		public ISound music;
		public MusicPropertyList properties;
		
		public OvertopMusic(ISound musicToSet, MusicPropertyList propToSet) {
			music = musicToSet;
			properties = propToSet;
		}
	}
	
	//Static helper methods

	public static boolean isSoundTracked(ISound sound) {
		return isBackgroundTracked(sound) || isOvertopTracked(sound) || isBattleTracked(sound);
	}
	
	public static boolean isBackgroundTracked(ISound sound) {
		for(BackgroundMusic music : MusicChoicesMusicTicker.ticker.backgroundQueue) {
			if(music.music == sound) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isBattleTracked(ISound sound) {
		for(BackgroundMusic music : MusicChoicesMusicTicker.ticker.battleQueue) {
			if(music.music == sound) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isOvertopTracked(ISound sound) {
		for(OvertopMusic overtop : MusicChoicesMusicTicker.ticker.overtopQueue) {
			if(overtop.music == sound) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isPlayingBossMusic() {
		return MusicChoicesMusicTicker.ticker.bossMusic != null || MusicChoicesMusicTicker.ticker.bossEntity != null;
	}
	
	public static boolean isPlayingBattleMusic() {
		return MusicChoicesMusicTicker.ticker.battleMusic != null || MusicChoicesMusicTicker.ticker.battleEntityType != null;
	}
	
	public static String getNameFromEntity(Entity entity) {
		//Fixes random crashyness
		if(entity == null) {
			return "null";
		}
		
		Class entityClass = entity.getClass();
		String entityName = (String) EntityList.classToStringMapping.get(entityClass);
		
		if(entity instanceof EntityPlayer) {
			entityName = "Player";
		}
		
		return entityName;
	}
	
	public static Class getEntityClassFromName(String name) {
		Class entityClass = (Class) EntityList.stringToClassMapping.get(name);
		
		if(name.equals("Player")) {
			entityClass = EntityPlayer.class;
		}
		
		return entityClass;
	}
	
	public static boolean isEntityInBattleRange(Entity entity) {
		return Math.abs(entity.posX - mc.thePlayer.posX) <= MusicChoicesMod.battleDistance && Math.abs(entity.posY - mc.thePlayer.posY) <= MusicChoicesMod.battleDistance && Math.abs(entity.posZ - mc.thePlayer.posZ) <= MusicChoicesMod.battleDistance;
	}
}
