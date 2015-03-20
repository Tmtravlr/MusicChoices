package com.tmtravlr.musicchoices;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/**
 * Event handler to play music when events happen.
 * 
 * @author Rebeca Rey
 * @Date Febuary 2015 
 */
public class MusicChoicesEventHandler {

//	private boolean isVanillaMusic(String name) {
//		return name.equals("minecraft:music.menu") || name.equals("minecraft:music.game") || name.equals("minecraft:music.game.creative") || name.equals("minecraft:music.game.end.credits") || name.equals("minecraft:music.game.nether") || name.equals("minecraft:music.game.end.dragon") || name.equals("minecraft:music.game.end");
//	}
	
	@SubscribeEvent
	public void onSound(PlaySoundEvent17 event) {
		
		if(event.category == SoundCategory.MUSIC || event.category == SoundCategory.RECORDS) {
			MusicChoicesMusicTicker ticker = MusicChoicesMod.ticker;
			
			if(event.result != null && !ticker.isSoundTracked(event.result)) {
				ticker.setOvertopMusic(event.result, null);
			}
		}
		
	}
	
//	//This is quite broad because I couldn't find another event for death that was fired on the client side.
//	@SubscribeEvent
//	public void onDeath(LivingDeathEvent event) {
//		if(!event.isCanceled() && event.entityLiving == Minecraft.getMinecraft().thePlayer) {
//			
//			if(!MusicProperties.deathList.isEmpty()) {
//				if(MusicChoicesMod.DEBUG) System.out.println("[Music Choices] Looking for death music to play.");
//				MusicProperties toPlay = MusicProperties.findTrackForCurrentSituationFromList(MusicProperties.deathList);
//				if(toPlay != null) {
//					MusicChoicesMod.ticker.playOvertopMusic(toPlay.location, toPlay.propertyList);
//				}
//			}
//			
//			MusicChoicesTickHandler.dead = true;
//		}
//	}
	
	
	
}
