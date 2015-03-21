package com.tmtravlr.musicchoices;

import java.util.*;

import com.tmtravlr.musicchoices.musicloader.MusicPropertyList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

/**
 * Music ticker based on the vanilla MusicTicker class which plays music based on the current
 * situation. This music ticker also tracks music playing over top of the background music, and
 * plays background music with "fade" support.
 * 
 * @author Rebeca Rey
 * @Date March 2015
 */
public class MusicChoicesMusicTicker extends MusicTicker {

	private static final Random rand = new Random();
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public LinkedList<BackgroundMusic> backgroundQueue = new LinkedList<BackgroundMusic>();
	public LinkedList<OvertopMusic> overtopQueue = new LinkedList<OvertopMusic>();
	
	public BackgroundMusic bossMusic;
	public EntityLivingBase bossEntity;
	
	public float globalBgFadeVolume = 1.0f;
	
	public int delay = 100;
	
	public MusicChoicesMusicTicker(Minecraft minecraft) {
		super(minecraft);
	}

	public void update() {
		MusicTicker.MusicType vanillaMusicType = this.mc.func_147109_W();

		//Check if the current music track is still valid

		String start = "";
		if(MusicChoicesMod.super_duper_debug) start = "Start: ( Delay: " + delay + ", Primary: " + !backgroundQueue.isEmpty()  + ", Overtop: " + !overtopQueue.isEmpty() + ") ";
		
		if(!backgroundQueue.isEmpty()) {
			
			boolean stillApplies = true;
			boolean primaryPlaying = true;
			
			Iterator it = backgroundQueue.iterator();
			while(it.hasNext()) {
				BackgroundMusic backMusic = (BackgroundMusic) it.next();
				
				//Check if the music currently playing still applies.
				if(backMusic.music.primary && !MusicProperties.checkIfMusicStillApplies(backMusic, vanillaMusicType)) {
					stillApplies = false;
				}
				
				//Update the fade volume
				
				backMusic.music.fadeVolume = this.globalBgFadeVolume;
				
				//Check if the sound is actually still playing
			
				if (!this.mc.getSoundHandler().isSoundPlaying(backMusic.music))
				{
					if(MusicChoicesMod.debug) System.out.println("[Music Choices] Background music stopped.");
					
					//If this is the primary music, stop all others
					if(backMusic.music.primary) {
						primaryPlaying = false;
					}
					
					it.remove();
					if(mc.currentScreen instanceof GuiMainMenu || mc.thePlayer == null) {
						this.delay = Math.min(MathHelper.getRandomIntegerInRange(this.rand, MusicChoicesMod.menuTickDelayMin/*vanillaMusicType.func_148634_b()*/, MusicChoicesMod.menuTickDelayMax/*vanillaMusicType.func_148633_c()*/), this.delay);
					}
					else {
						this.delay = Math.min(MathHelper.getRandomIntegerInRange(this.rand, MusicChoicesMod.ingameTickDelayMin/*vanillaMusicType.func_148634_b()*/, MusicChoicesMod.ingameTickDelayMax/*vanillaMusicType.func_148633_c()*/), this.delay);
					}
				}
			}
			
			if(!stillApplies) {
				playBackgroundMusic();
			}
			
			//If not playing primary music, stop all others too.
			if(!primaryPlaying) {
				it = backgroundQueue.iterator();
				while(it.hasNext()) {
					BackgroundMusic backMusic = (BackgroundMusic) it.next();
					mc.getSoundHandler().stopSound(backMusic.music);
					it.remove();
				}
			}
		}
		
		//Handle the overtop music stopping
		
		if(!overtopQueue.isEmpty()) {
			Iterator it = overtopQueue.iterator();
			while(it.hasNext()) {
				OvertopMusic overtop = (OvertopMusic) it.next();
				
				if(!this.mc.getSoundHandler().isSoundPlaying(overtop.music)) {
					if(MusicChoicesMod.debug) System.out.println("[Music Choices] Stopping overtop music track.");
					
					it.remove();
				}
				
			}
		}
		
		//Handle new music playing; however, only play if there is no music playing over top.
		
		if(overtopQueue.isEmpty()) {
			
			this.globalBgFadeVolume = 1.0f;
			
			if (backgroundQueue.isEmpty() && this.delay-- <= 0) {
				this.delay = Integer.MAX_VALUE;
				
				playBackgroundMusic();
			}
		}
		
		//Handle "boss" music
		
		if(bossMusic != null) {
			if((bossEntity == null || bossEntity.isDead || bossEntity.getHealth() <= 0)) {
				//Stop the boss music and play the victory music
				bossMusic.music.fadeVolume = 0.0f;
				
				if(bossEntity != null && bossEntity.getHealth() <= 0) {
					//Play victory music
					
					MusicProperties victory = MusicProperties.findBossVictoryMusic(bossEntity);
					
					if(victory != null) {
						this.playOvertopMusic(victory);
					}
				}
				
				bossEntity = null;
			}
			
			if(!mc.getSoundHandler().isSoundPlaying(this.bossMusic.music)) {
				this.bossMusic = null;
			}
		}
		
		if(MusicChoicesMod.super_duper_debug) System.out.println(start + "End: ( Delay: " + delay + ", Primary: " + !backgroundQueue.isEmpty()  + ", Overtop: " + !overtopQueue.isEmpty() + ") ");

	}
	
	public void playBossMusic(MusicProperties prop) {
		MusicTickable toPlay = new MusicTickable(prop.location);
		
		this.bossMusic = new BackgroundMusic(toPlay, prop.propertyList);
		setOvertopMusic(toPlay, prop.propertyList);
		
		if(MusicChoicesMod.debug) System.out.println("[Music Choices] Playing boss music track called " + toPlay.getPositionedSoundLocation());
		this.mc.getSoundHandler().playSound(toPlay);
	}
	
	public void playBackgroundMusic() {
		MusicTicker.MusicType vanillaMusicType = this.mc.func_147109_W();
		
		//Fade out any other tracks playing
		
		if(!backgroundQueue.isEmpty()) {
			Iterator it = backgroundQueue.iterator();
			
			while(it.hasNext()) {
				BackgroundMusic backMusic = (BackgroundMusic) it.next();
				
				backMusic.music.primary = false;
			}
		}
		
		boolean foundCurrentlyPlaying = false;
		
		//Search for a track currently playing that now applies
		
		if(!backgroundQueue.isEmpty()) {
			Iterator it = backgroundQueue.iterator();
			
			while(it.hasNext()) {
				BackgroundMusic backMusic = (BackgroundMusic) it.next();
				
				if(MusicProperties.checkIfMusicStillApplies(backMusic, vanillaMusicType)) {
					backMusic.music.primary = true;
					foundCurrentlyPlaying = true;
				}
			}
		}
		
		if(!foundCurrentlyPlaying) {
			
			//Remove the first music which isn't playing if we reached our limit
			
			if(backgroundQueue.size() >= MusicChoicesMod.maxBackground) {
				Iterator it = backgroundQueue.iterator();
				
				while(it.hasNext()) {
					BackgroundMusic backMusic = (BackgroundMusic) it.next();
					
					if(!backMusic.music.primary) {
						mc.getSoundHandler().stopSound(backMusic.music);
						it.remove();
						break;
					}
				}
				
			}
		
			MusicProperties musicProperties = MusicProperties.findTrackForCurrentSituation();
			ResourceLocation location = vanillaMusicType.getMusicTickerLocation();
			
			if (musicProperties != null && musicProperties.location != null) {
				location = musicProperties.location;
			}
			
			BackgroundMusic toPlay = new BackgroundMusic(new MusicTickable(location), null);
			
			if(musicProperties != null && musicProperties.propertyList != null) {
				toPlay.properties = musicProperties.propertyList;
			}
			
			if(MusicChoicesMod.debug) System.out.println("Playing music track called " + toPlay.music.getPositionedSoundLocation());
			backgroundQueue.addLast(toPlay);
			this.mc.getSoundHandler().playSound(toPlay.music);
			
		}
	}
	
	public void playOvertopMusic(MusicProperties prop) {
		MusicTickable toPlay = new MusicTickable(prop.location);
		
		setOvertopMusic(toPlay, prop.propertyList);
		
		if(MusicChoicesMod.debug) System.out.println("[Music Choices] Playing over-top music track called " + toPlay.getPositionedSoundLocation());
		this.mc.getSoundHandler().playSound(toPlay);
	}
	
	public boolean setOvertopMusic(ISound sound, MusicPropertyList properties) {
		MusicTicker.MusicType vanillaMusicType = this.mc.func_147109_W();
		
		//Stop the current background music if not set to overlap
		
		boolean canPlay = true;
		
		if(properties == null || !properties.overlap) {
			this.globalBgFadeVolume = 0.001f;
			
			if(this.overtopQueue.size() >= MusicChoicesMod.maxOvertop) {
				//We reached the limit! Replace the oldest one.
				OvertopMusic toRemove = overtopQueue.removeFirst();
				
				if(mc.getSoundHandler().isSoundPlaying(toRemove.music)) {
					mc.getSoundHandler().stopSound(toRemove.music);
				}
			}
		}
		else {
			this.globalBgFadeVolume = Math.min(MusicChoicesMod.backgroundFade, this.globalBgFadeVolume);
		}
		
		//Add the music track to the end of the queue.
		
		OvertopMusic toPlay = new OvertopMusic(sound, properties);
		overtopQueue.addLast(toPlay);
		if(MusicChoicesMod.debug) System.out.println("[Music Choices] Tracking over-top music track called " + sound.getPositionedSoundLocation());
		
		return true;
	}
	
	public boolean isSoundTracked(ISound sound) {
		return isBackgroundTracked(sound) || isOvertopTracked(sound);
	}
	
	public boolean isBackgroundTracked(ISound sound) {
		for(BackgroundMusic music : backgroundQueue) {
			if(music.music == sound) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isOvertopTracked(ISound sound) {
		for(OvertopMusic overtop : overtopQueue) {
			if(overtop.music == sound) {
				return true;
			}
		}
		return false;
	}
	
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

}
