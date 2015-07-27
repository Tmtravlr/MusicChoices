package com.tmtravlr.musicchoices;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Event handler to play music when events happen.
 * 
 * @author Rebeca Rey
 * @Date Febuary 2015 
 */
public class MusicChoicesEventHandler {
	
	private Minecraft mc = Minecraft.getMinecraft();
	private Random rand = new Random();
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onSound(PlaySoundEvent17 event) {
		
		//Set any 
		if((event.result != null && event.category == SoundCategory.MUSIC || event.category == SoundCategory.RECORDS) && !event.result.getPositionedSoundLocation().toString().contains("note.")) {
			
			if(!MChHelper.isSoundTracked(event.result)) {
				MusicChoicesMusicTicker.ticker.setOvertopMusic(event.result, null);
			}
		}
	}
	
	/**
	 * Plays the credits music when the credits GUI is opened.
	 */
	@SubscribeEvent
	public void onCredits(GuiOpenEvent event) {
		if (event.gui instanceof GuiWinGame && MusicChoicesMusicTicker.ticker.creditsMusic == null) {
			
			if(!MusicProperties.creditsList.isEmpty()) {
				
				MusicProperties credits = MusicProperties.creditsList.get(rand.nextInt(MusicProperties.creditsList.size()));
				
				if(credits != null) {
					MusicChoicesMusicTicker.ticker.playCreditsMusic(credits);
				}
			}
		}
	}
	
//	@SubscribeEvent
//	public void onTargetting(LivingSetAttackTargetEvent event) {
//		triggerBattleMusic(event.entityLiving, event.target);
//	}
	
//	@SubscribeEvent
//	public void onLivingAttacked(LivingAttackEvent event) {
//		
//		System.out.println("Attack detected! Attacker: "+event.source.getEntity()+", Target: "+event.entityLiving);
//		
//		if(event.source.getEntity() instanceof EntityLivingBase) {
//			triggerBattleMusic((EntityLivingBase) event.source.getEntity(), event.entityLiving);
//		}
//	}
	
	@SubscribeEvent
	public void onAttack(AttackEntityEvent event) {
		
		if(event.target instanceof EntityLivingBase) {
			triggerBattleMusic(event.entityLiving, (EntityLivingBase) event.target);
		}
	}
	
	private void triggerBattleMusic(EntityLivingBase attacker, EntityLivingBase target) {
		
		//Don't bother checking if there is no target
		if(attacker == null) {
			return;
		}
		
		//First try to play boss music
		if(!MChHelper.isPlayingBossMusic()) {
			//First check the entity getting hit 
			if(target != mc.thePlayer && attacker != null && attacker instanceof EntityPlayer) {
				if(playBossMusicForEntity(target)) {
					return;
				}
			}
			
			//Next check the entity hitting
			if(target instanceof EntityPlayer && attacker != null && attacker instanceof EntityLivingBase && attacker != mc.thePlayer) {
				if(playBossMusicForEntity((EntityLivingBase) attacker)) {
					return;
				}
			}
		}
		
		//Then if nothing plays there, try to play battle music.
		if(!MChHelper.isPlayingBattleMusic() && !MChHelper.isPlayingBossMusic()) {
			//First check the entity getting hit 
			if(target != mc.thePlayer && attacker != null && attacker instanceof EntityPlayer) {
				if(playBattleMusicForEntity(target)) {
					return;
				}
			}
			
			//Next check the entity hitting
			if(target instanceof EntityPlayer && attacker != null && attacker instanceof EntityLivingBase && attacker != mc.thePlayer) {
				if(playBattleMusicForEntity((EntityLivingBase) attacker)) {
					return;
				}
			}
		}
	}
	
	private boolean playBossMusicForEntity(EntityLivingBase entity) {
		if(entity != null && !entity.isDead && entity.getHealth() > 0) {
			MusicProperties toPlay = MusicProperties.findMusicFromNBTMap(entity, MusicProperties.bossMap);
			
			if(toPlay != null) {
				MusicChoicesMusicTicker.ticker.playBossMusic(toPlay);
				MusicChoicesMusicTicker.ticker.bossEntity = entity;
				return true;
			}
		}
		
		return false;
	}
	
	private boolean playBattleMusicForEntity(EntityLivingBase entity) {
		String entityName = MChHelper.getNameFromEntity(entity);
		
		if(MusicChoicesMod.debug) System.out.println("[Music Choices] Searching for battle music for " + entityName);
		
		if(!entity.isDead && entity.getHealth() > 0 && MChHelper.isEntityInBattleRange(entity)) {
			MusicProperties toPlay = MusicProperties.findMusicFromStringMap(entityName, MusicProperties.battleMap);
			
			if(toPlay == null) {
				if(!MusicChoicesMod.battleMonsterOnly || entity.isCreatureType(EnumCreatureType.monster, false)) {
					toPlay = MusicProperties.findBattleMusicFromBlacklist(entityName);
				}
			}
			
			if(toPlay != null) {
				MusicChoicesMusicTicker.ticker.playBattleMusic(toPlay);
				MusicChoicesMusicTicker.ticker.battleEntityType = entityName;
				return true;
			}
			
		}
		
		return false;
	}
	
}
