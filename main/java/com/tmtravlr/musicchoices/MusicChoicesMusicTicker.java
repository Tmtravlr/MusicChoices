package com.tmtravlr.musicchoices;

import java.util.*;

import com.tmtravlr.musicchoices.musicloader.MusicPropertyList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;
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

	private static final Random RAND = new Random();
	private static final Minecraft MC = Minecraft.getMinecraft();
	
	public LinkedList<BackgroundMusic> backgroundQueue = new LinkedList<BackgroundMusic>();
	public LinkedList<OvertopMusic> overtopQueue = new LinkedList<OvertopMusic>();
	
	public float globalBgFadeVolume = 1.0f;
	
	public int delay = 100;
	
	/***** Properties! ******/
	
	/** Maximum number of "background" tracks that can play at once. */
	public static int maxBackground = 3;
	
	/** Maximum number of "overtop" tracks that can play at once that don't have overlap set to true. */
	public static int maxOvertop = 1;
	
	/** How much the background music should fade when music plays over top of it. */
	public static float backgroundFade = 0.4f;
	
	/** Tick delay for the menu music */
	public static int menuTickDelayMin = -1;
	public static int menuTickDelayMax = -1;
	
	/** Tick delay for all ingame music */
	public static int ingameTickDelayMin = -1;
	public static int ingameTickDelayMax = -1;
	
	/************************/
	
	public MusicChoicesMusicTicker(Minecraft minecraft) {
		super(minecraft);
	}

	public void update() {
		MusicTicker.MusicType vanillaMusicType = this.MC.func_147109_W();

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
			
				if (!this.MC.getSoundHandler().isSoundPlaying(backMusic.music))
				{
					if(MusicChoicesMod.debug) System.out.println("[Music Choices] Background music stopped.");
					
					//If this is the primary music, stop all others
					if(backMusic.music.primary) {
						primaryPlaying = false;
					}
					
					it.remove();
					this.delay = Math.min(MathHelper.getRandomIntegerInRange(this.RAND, vanillaMusicType.func_148634_b(), vanillaMusicType.func_148633_c()), this.delay);
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
					MC.getSoundHandler().stopSound(backMusic.music);
					it.remove();
				}
			}
		}
		
		//Handle the overtop music stopping
		
		if(!overtopQueue.isEmpty()) {
			Iterator it = overtopQueue.iterator();
			while(it.hasNext()) {
				OvertopMusic overtop = (OvertopMusic) it.next();
				
				if(!this.MC.getSoundHandler().isSoundPlaying(overtop.music)) {
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
		
	if(MusicChoicesMod.super_duper_debug) System.out.println(start + "End: ( Delay: " + delay + ", Primary: " + !backgroundQueue.isEmpty()  + ", Overtop: " + !overtopQueue.isEmpty() + ") ");

	}
	
	public void playBackgroundMusic() {
		MusicTicker.MusicType vanillaMusicType = this.MC.func_147109_W();
		
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
			
			if(backgroundQueue.size() >= maxBackground) {
				Iterator it = backgroundQueue.iterator();
				
				while(it.hasNext()) {
					BackgroundMusic backMusic = (BackgroundMusic) it.next();
					
					if(!backMusic.music.primary) {
						MC.getSoundHandler().stopSound(backMusic.music);
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
			this.MC.getSoundHandler().playSound(toPlay.music);
			
		}
	}
	
	public void playOvertopMusic(ResourceLocation location, MusicPropertyList properties) {
		MusicTickable toPlay = new MusicTickable(location);
		
		setOvertopMusic(toPlay, properties);
		
		if(MusicChoicesMod.debug) System.out.println("[Music Choices] Playing over-top music track called " + toPlay.getPositionedSoundLocation());
		this.MC.getSoundHandler().playSound(toPlay);
	}
	
	public boolean setOvertopMusic(ISound sound, MusicPropertyList properties) {
		MusicTicker.MusicType vanillaMusicType = this.MC.func_147109_W();
		
		//Stop the current background music if not set to overlap
		
		boolean canPlay = true;
		
		if(properties == null || !properties.overlap) {
			this.globalBgFadeVolume = 0.001f;
			
			if(this.overtopQueue.size() >= maxOvertop) {
				//We reached the limit! Replace the oldest one.
				OvertopMusic toRemove = overtopQueue.removeFirst();
				
				if(MC.getSoundHandler().isSoundPlaying(toRemove.music)) {
					MC.getSoundHandler().stopSound(toRemove.music);
				}
			}
		}
		else {
			this.globalBgFadeVolume = Math.min(this.backgroundFade, this.globalBgFadeVolume);
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
