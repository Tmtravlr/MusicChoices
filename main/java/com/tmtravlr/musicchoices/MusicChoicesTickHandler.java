package com.tmtravlr.musicchoices;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

/**
 * Tick handler to handle things that need to be updated per tick.
 * 
 * @author Rebeca Rey
 * @Date Febuary 2015 
 */
public class MusicChoicesTickHandler {
	
	private static Minecraft mc = Minecraft.getMinecraft();
	private static Random rand = new Random();
	
	public static boolean dead = false;
	//A negative value means that it needs resetting
	public static float sunBrightness = -1.0f;
	public static int dimensionId = 0;
	
	//Cooldown for the music so it doesn't check every tick
	private int achievementCooldown = 10;
	private int dayCheckCooldown = 9;
	private int deathCooldown = 8;
	
	public int menuTickDelay = 10;

	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		
		//Handle Achievements
		
		if(mc.theWorld != null && mc.thePlayer != null && mc.thePlayer.getStatFileWriter() != null) {
			
			if(!MusicChoicesMod.worldLoaded) {
				
				for (Object a : AchievementList.achievementList)
				{
					Achievement ach = (Achievement)a;
	
					if (mc.thePlayer.getStatFileWriter().hasAchievementUnlocked(ach)) {
						MusicChoicesMod.achievementsUnlocked.put(ach, true);
					}
					else {
						MusicChoicesMod.achievementsUnlocked.put(ach, false);
					}
				}
				
				if(!MusicProperties.loginList.isEmpty()) {
					if(MusicChoicesMod.debug) System.out.println("[Music Choices] Looking for login music to play.");
					MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.loginList);
					if(toPlay != null) {
						MusicChoicesMod.ticker.playOvertopMusic(toPlay.location, toPlay.propertyList);
					}
				}
				
				MusicChoicesMod.worldLoaded = true;
			}
			
			if (this.achievementCooldown-- <= 0)
			{
				this.achievementCooldown = 10;
	
				for (Object a : AchievementList.achievementList)
				{
					Achievement ach = (Achievement)a;
	
					if (mc.thePlayer.getStatFileWriter().hasAchievementUnlocked(ach) && !((Boolean)MusicChoicesMod.achievementsUnlocked.get(ach)).booleanValue())
					{
						if(MusicChoicesMod.debug) System.out.println("[Music Choices] Looking for achievement music for achievement " + ach.statId);
						
						MusicChoicesMod.achievementsUnlocked.put(ach, Boolean.valueOf(true));
						MusicProperties toPlay = MusicProperties.findTrackForAchievement(ach.statId);
	
						if (toPlay != null)
						{
							MusicChoicesMod.ticker.playOvertopMusic(toPlay.location, toPlay.propertyList);
						}
					}
				}
			}
			
			if(dayCheckCooldown-- <= 0) {
				dayCheckCooldown = 10;
				
				if(dimensionId != mc.theWorld.provider.dimensionId) {
					//We changed dimensions, so reset the brightness
					sunBrightness = -1.0F;
					dimensionId = mc.theWorld.provider.dimensionId;
					dayCheckCooldown = 100;
				}
				else {
					if(sunBrightness >= 0) {
						if(sunBrightness > 0.5F && mc.theWorld.getSunBrightness(1.0F) < 0.5F) {
							//It went from day to night
							if(!MusicProperties.sunsetList.isEmpty()) {
								if(MusicChoicesMod.debug) System.out.println("[Music Choices] Looking for sunset music to play.");
								MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.sunsetList);
								if(toPlay != null) {
									MusicChoicesMod.ticker.playOvertopMusic(toPlay.location, toPlay.propertyList);
								}
							}
						}
						if(sunBrightness < 0.5F && mc.theWorld.getSunBrightness(1.0F) > 0.5F) {
							//It went from night to day
							if(!MusicProperties.sunriseList.isEmpty()) {
								if(MusicChoicesMod.debug) System.out.println("[Music Choices] Looking for sunrise music to play.");
								MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.sunriseList);
								if(toPlay != null) {
									MusicChoicesMod.ticker.playOvertopMusic(toPlay.location, toPlay.propertyList);
								}
							}
						}
					}
					
					sunBrightness = mc.theWorld.getSunBrightness(1.0F);
				}
			}
			
			//Handle death music
			
			if(deathCooldown-- <= 0) {
				deathCooldown = 10;
				
				if(!dead && mc.thePlayer.isDead) {
					dead = true;
					
					if(!MusicProperties.deathList.isEmpty()) {
						if(MusicChoicesMod.debug) System.out.println("[Music Choices] Looking for death music to play.");
						MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.deathList);
						if(toPlay != null) {
							MusicChoicesMod.ticker.playOvertopMusic(toPlay.location, toPlay.propertyList);
						}
					}
				}
				else if(dead && !mc.thePlayer.isDead) {
					dead = false;
					
					if(!MusicProperties.respawnList.isEmpty()) {
						if(MusicChoicesMod.debug) System.out.println("[Music Choices] Looking for respawn music to play.");
						MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.respawnList);
						if(toPlay != null) {
							MusicChoicesMod.ticker.playOvertopMusic(toPlay.location, toPlay.propertyList);
						}
					}
				}
			}
			
		}
		else {
			MusicChoicesMod.worldLoaded = false;
		}
	}
	
}
