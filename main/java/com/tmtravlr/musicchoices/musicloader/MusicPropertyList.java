package com.tmtravlr.musicchoices.musicloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import scala.actors.threadpool.Arrays;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundList;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * List to hold info about the music properties.
 * 
 * @author Rebeca Rey
 * @Date Febuary 2015 
 */
public class MusicPropertyList {

	//Music Options
	
	//Whether or not this is mean to be played with Music Choices
	public boolean isMusic = false;
	
	//Whether or not it should overlap with other music tracks
	public boolean overlap = false;

	//If the music should play in the menu
	public boolean menu = false;

	//If the music should play in the credits
	public boolean credits = false;

	//Which bosses the boss battle music should play for
	public HashSet<NBTTagCompound> bossTags = new HashSet<NBTTagCompound>();

	//Which bosses the victory music should play for
	public HashSet<NBTTagCompound> victoryTags = new HashSet<NBTTagCompound>();

	//Play for all achievements?
	public boolean allAchievements = false;

	//Set of specific achievements this music should play for
	public HashSet<String> achievements = new HashSet<String>();

	//Specific event to play this music at
	public String event = null;

	//Whether the music should play when in creative mode (if allGamemodes is true, it will play regardless)
	public boolean allGamemodes = true;
	public boolean creative = true;
	
	//Which biomes you should be in for this music to play
	public HashSet<String> biomes = null;
	
	//Which biome types you should be in for this music to play
	public HashSet<String> biomeTypes = null;
	
	//Which dimensions you should be in for this music to play
	public HashSet<Integer> dimensions = null;
	
	//Which dimensions you should _not_ be in for this music to play
	public HashSet<Integer> dimensionBlacklist = new HashSet<Integer>();

	//Which lighting you should be in for this music to play
	public HashSet<String> lighting = null;
	
	//What weather the music will play in
	public HashSet<String> weather = null;
	
	//The minimum height the music will play at
	public int heightMin = Integer.MIN_VALUE;
	
	//The maximum height the music will play at
	public int heightMax = Integer.MAX_VALUE;
	
	//Entities you should be near for this music to play
	public HashSet<String> entities = new HashSet<String>();
	
	//Blocks you should be near for this music to play
	public HashSet<String> blocks = new HashSet<String>();
	
	
	
	//Options entries
	
	//Whether this is an options entry
	public boolean isOptions;
	
	/** Maximum number of "background" tracks that can play at once. */
	public int maxBackground = -1;
	
	/** Maximum number of "overtop" tracks that can play at once that don't have overlap set to true. */
	public int maxOvertop = -1;
	
	/** How much the background music should fade when music plays over top of it. */
	public float backgroundFade = -1.0f;
	
	/** How fast the background music fades */
	public int fadeStrength = -1;
	
	/** Tick delay for the menu music */
	public int menuTickDelayMin = -1;
	public int menuTickDelayMax = -1;
	
	/** Tick delay for all ingame music */
	public int ingameTickDelayMin = -1;
	public int ingameTickDelayMax = -1;
	
}
