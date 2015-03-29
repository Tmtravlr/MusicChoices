package com.tmtravlr.musicchoices;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

/**
 * A tickable sound which has support for "fades"; created for background music.
 * @author Rebeca Rey
 * @Date March 2015
 */
public class MusicTickable extends PositionedSound implements ITickableSound {

	//Used for fades
	public boolean primary;
	public float fadeVolume;
	
	public MusicTickable(ResourceLocation location) {
		this(location, false);
	}
	
	public MusicTickable(ResourceLocation location, boolean repeat) {
		this(location, 1.0F, 1.0F, repeat, 0, ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F);
	}
	
	public MusicTickable(ResourceLocation location, float volume, float pitch, boolean repeat, int repeatDelay, ISound.AttenuationType attenuation, float posX, float posY, float posZ) {
		super(location);
		
		this.volume = volume;
		this.primary = true;
		this.fadeVolume = volume;
		this.field_147663_c = pitch;
		this.repeat = repeat;
		this.field_147665_h = repeatDelay;
		this.field_147666_i = attenuation;
		this.xPosF = posX;
		this.yPosF = posY;
		this.zPosF = posZ;
	}
	
	public static MusicTickable copyFrom(ISound sound) {
		return new MusicTickable(sound.getPositionedSoundLocation(), sound.getVolume(), sound.getPitch(), sound.canRepeat(), sound.getRepeatDelay(), sound.getAttenuationType(), sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
	}

	@Override
	public void update() {
		fadeVolume = MathHelper.clamp_float(fadeVolume, 0.0F, 1.0F);
		float primaryVolume = primary ? 1.0f : 0.0001f;
		if(MusicChoicesMod.super_duper_debug) System.out.println("Volume: " + volume + ", Fade volume: " + fadeVolume + ", Primary volume: " + primaryVolume);
		
		if(Math.abs(volume - Math.min(fadeVolume, primaryVolume)) >= 0.0001f) {
			volume = (volume*MusicChoicesMod.fadeStrength + Math.min(primaryVolume, fadeVolume)) / (MusicChoicesMod.fadeStrength + 1);
		}
		
	}

	@Override
	public boolean isDonePlaying() {
		return volume < 0.0001f || !MChHelper.isSoundTracked(this);
	}

}
