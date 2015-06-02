package com.tmtravlr.musicchoices;

import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
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
	private int bossCooldown = 7;
	
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
						MusicChoicesMusicTicker.ticker.playOvertopMusic(toPlay);
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
							MusicChoicesMusicTicker.ticker.playOvertopMusic(toPlay);
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
									MusicChoicesMusicTicker.ticker.playOvertopMusic(toPlay);
								}
							}
						}
						if(sunBrightness < 0.5F && mc.theWorld.getSunBrightness(1.0F) > 0.5F) {
							//It went from night to day
							if(!MusicProperties.sunriseList.isEmpty()) {
								if(MusicChoicesMod.debug) System.out.println("[Music Choices] Looking for sunrise music to play.");
								MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.sunriseList);
								if(toPlay != null) {
									MusicChoicesMusicTicker.ticker.playOvertopMusic(toPlay);
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
							MusicChoicesMusicTicker.ticker.playOvertopMusic(toPlay);
						}
					}
				}
				else if(dead && !mc.thePlayer.isDead) {
					dead = false;
					
					if(!MusicProperties.respawnList.isEmpty()) {
						if(MusicChoicesMod.debug) System.out.println("[Music Choices] Looking for respawn music to play.");
						MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.respawnList);
						if(toPlay != null) {
							MusicChoicesMusicTicker.ticker.playOvertopMusic(toPlay);
						}
					}
				}
			}
			
			//Boss music
			if(!MChHelper.isPlayingBossMusic() && !MusicProperties.bossMap.isEmpty() && bossCooldown-- <= 0) {
				bossCooldown = 10;
				
				//See what entity the player is looking at, and play boss music if applicable. 
				//MovingObjectPosition mop = mc.objectMouseOver;//this.mc.renderViewEntity.rayTrace(1000, 0.0f);
				
				Entity lookedAt = findEntityLookedAt();
				
				if(lookedAt != null && lookedAt instanceof EntityLivingBase) {//mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mop.entityHit instanceof EntityLivingBase) {
					if(MusicChoicesMod.super_duper_debug) System.out.println("[Music Choices] Entity looked at is " + lookedAt + ", with id " + EntityList.getEntityString(lookedAt));
					
					EntityLivingBase entity = (EntityLivingBase) lookedAt;
					
					if(!entity.isDead && entity.getHealth() > 0) {
						MusicProperties toPlay = MusicProperties.findMusicFromNBTMap(entity, MusicProperties.bossMap);
						
						if(toPlay != null) {
							MusicChoicesMusicTicker.ticker.playBossMusic(toPlay);
							MusicChoicesMusicTicker.ticker.bossEntity = entity;
						}
					}
				}
			}
			
		}
		else {
			MusicChoicesMod.worldLoaded = false;
		}
		
		
	}
	
	private Entity findEntityLookedAt() {
		int distance = 1000;
		
		Vec3 vecPos = this.mc.renderViewEntity.getPosition(0);
		Vec3 vecLook = this.mc.renderViewEntity.getLook(0);
        Vec3 vecPosLook = vecPos.addVector(vecLook.xCoord * distance, vecLook.yCoord * distance, vecLook.zCoord * distance);
        Entity pointedEntity = null;
        Vec3 vecHit = null;
        float expansion = 1.0F;
        List entityList = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(this.mc.renderViewEntity, this.mc.renderViewEntity.boundingBox.addCoord(vecLook.xCoord * distance, vecLook.yCoord * distance, vecLook.zCoord * distance).expand((double)expansion, (double)expansion, (double)expansion));
        double d2 = distance;

        for (int i = 0; i < entityList.size(); ++i)
        {
            Entity entity = (Entity)entityList.get(i);

            if (entity.canBeCollidedWith())
            {
                float f2 = entity.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity.boundingBox.expand((double)f2, (double)f2, (double)f2);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vecPos, vecPosLook);

                if (axisalignedbb.isVecInside(vecPos))
                {
                    if (0.0D < d2 || d2 == 0.0D)
                    {
                        pointedEntity = entity;
                        vecHit = movingobjectposition == null ? vecPos : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                }
                else if (movingobjectposition != null)
                {
                    double d3 = vecPos.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D)
                    {
                        if (entity == this.mc.renderViewEntity.ridingEntity && !entity.canRiderInteract())
                        {
                            if (d2 == 0.0D)
                            {
                                pointedEntity = entity;
                                vecHit = movingobjectposition.hitVec;
                            }
                        }
                        else
                        {
                            pointedEntity = entity;
                            vecHit = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }
        }
        
        return pointedEntity;
	}
	
}
