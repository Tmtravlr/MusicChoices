package com.tmtravlr.musicchoices.musicloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import scala.actors.threadpool.Arrays;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundList;

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

	//Whether or not this is mean to be played with Music Choices
	public boolean valid = false;
	
	//Whether or not it should overlap with other music tracks
	public boolean overlap = false;

	//If the music should play in the menu
	public boolean menu = false;

	//If the music should play in the credits
	public boolean credits = false;

	//Which bosses the boss battle music should play for
	public HashMap<String, String> bossMap = new HashMap<String, String>();

	//Which bosses the victory music should play for
	public HashMap<String, String> victoryMap = new HashMap<String, String>();

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

}
