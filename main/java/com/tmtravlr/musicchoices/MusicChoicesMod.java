package com.tmtravlr.musicchoices;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import net.minecraftforge.common.BiomeDictionary.Type;

@Mod(
		modid = "musicchoices",
		name = "Music Choices",
		version = "0.5"
		)
@NetworkMod(
		clientSideRequired = true,
		serverSideRequired = false
		)
public class MusicChoicesMod implements ITickHandler
{
	@Instance("musicchoices")
	public static MusicChoicesMod musicChoices;

	@SidedProxy(
			clientSide = "com.tmtravlr.musicchoices.ClientProxy",
			serverSide = "com.tmtravlr.musicchoices.CommonProxy"
			)
	public static CommonProxy proxy;

	public static String worldName;

	//The music folder inside the config folder
	public static String musicFolderName;

	//These hold how many ticks before something should happen
	private int ticksBeforeMenuMusic = 0;
	private int bossMusicTickdown = 5;
	private int achievementMusicTickdown = 10;

	//When an achievement gets set to true in this map,
	//the corresponding achievement music will try to play
	private Map<Achievement, Boolean> achievementsUnlocked = new HashMap();
	private boolean achievementsLoaded = false;

	//Config options
	private static int MENU_MUSIC_INTERVAL = 100;
	private static int INGAME_MUSIC_INTERVAL = 12000;
	private static boolean LET_ACH_MUSIC_OVERLAP = true;
	private static boolean PLAY_VICTORY_MUSIC = false;
	private static boolean CANCEL_BOSS_MUSIC = false;
	private static boolean IMMEDIATE_BOSS_MUSIC = false;
	private static boolean REGENERATE_FOLDER = false;

	public static boolean ONLY_BOSS_MUSIC_FOR_BOSSES = false;

	public static boolean DEBUG = false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		//Get the config file and the music folder
		File musicChoicesFolder = new File(event.getSuggestedConfigurationFile().getParentFile(), "MusicChoices");
		musicFolderName = musicChoicesFolder.getParentFile().getName() + "\\MusicChoices";

		Configuration config = new Configuration(new File(musicChoicesFolder, "musicChoicesConfig.cfg"));


		config.load();

		config.addCustomCategoryComment("delays", "These control the delays in ticks from when one music\ntrack ends to when the next begins. Note that for the\nIngame delay, it will be a random number between the given\ndelay and double that number (so if you have 12000, which is\nthe default, it will be something between 12000 and 24000).\nAlso, the Ingame delay will only affect the delay after\nthe first in-game music track has played.");
		MENU_MUSIC_INTERVAL = config.get("delays", "Tick Delay between Menu Music Tracks", 100).getInt();
		INGAME_MUSIC_INTERVAL = config.get("delays", "Tick Delay between Ingame Music Tracks", 12000).getInt();
		Property temp = config.get("boss_music", "Play boss music immediately?", false);
		temp.comment = "If set to true, will immediately play the boss music\n(from the \'Bosses\' folder, or a sub-folder with the boss\' name)\n when the boss\' health bar appears.";
		IMMEDIATE_BOSS_MUSIC = temp.getBoolean(false);
		temp = config.get("boss_music", "Play victory jingle when boss dies?", false);
		temp.comment = "If set to true, when the boss health bar reaches 0,\nit will cancel the music playing and play a file from the\n\'Victory\' folder.";
		PLAY_VICTORY_MUSIC = temp.getBoolean(false);
		temp = config.get("boss_music", "Cancel boss music when boss health bar goes away?", false);
		temp.comment = "Stops the current music playing when the boss health bar\ndisappears. Works best with \'Play boss music immediately\'.";
		CANCEL_BOSS_MUSIC = temp.getBoolean(false);
		temp = config.get("boss_music", "Play only boss music when boss health bar present?", false);
		temp.comment = "If set to true, when the boss health bar is on the\nscreen, ONLY boss music relevant to that boss will play\n(not other possible music). Always true if\n\'Play boss music immediately\' is true.";
		ONLY_BOSS_MUSIC_FOR_BOSSES = temp.getBoolean(false);
		config.addCustomCategoryComment("achievements", "If false, achievement music that plays will stop the\nbackground music. If true, achievement music will\n play over top of it.");
		LET_ACH_MUSIC_OVERLAP = config.get("achievements", "Let the acheivement music overlap the background music?", true).getBoolean(true);
		config.addCustomCategoryComment("debug", "Set this to true to write debug information in the console.");
		DEBUG = config.get("debug", "Debug", false).getBoolean(false);
		config.addCustomCategoryComment("folder", "Set to true to regenerate the music folders each time you \nload a world. Note this could take a while. You should\nset this to true if you update the mod or add new biomes \nor dimensions, then set back to false after.");
		REGENERATE_FOLDER = config.get("folder", "Regenerate folders when loading world?", false).getBoolean(false);

		config.save();


		proxy.registerEventHandlers();
		TickRegistry.registerTickHandler(this, Side.CLIENT);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {}

	@SideOnly(Side.CLIENT)
	private void loadMusic()
	{
		if (DEBUG)
		{
			System.out.println("[Music Choices] Loading world and generating music folders.");
		}

		if (DEBUG)
		{
			System.out.println("[Music Choices] Stopping the menu music.");
		}

		//Set the music interval for in-game music
		Minecraft.getMinecraft().sndManager.sndSystem.stop("BgMusic");
		this.ticksBeforeMenuMusic = 0;
		SoundManager.MUSIC_INTERVAL = INGAME_MUSIC_INTERVAL;

		//Generate the music folders if they don't exist
		try
		{
			File musicFolder = new File(musicFolderName);

			if (!musicFolder.exists())
			{
				musicFolder.mkdir();
			}
			else if (new File(musicFolder, "Lighting").exists() && !REGENERATE_FOLDER)
			{
				return;
			}

			if (DEBUG)
			{
				System.out.println("[Music Choices] Generating the main folder.");
			}

			File menuMusicDirectory = new File(musicFolder, "Menu");

			if (!menuMusicDirectory.exists())
			{
				menuMusicDirectory.mkdir();
			}

			if (DEBUG)
			{
				System.out.println("[Music Choices] Generating the \'Menu\' music folder.");
			}

			File creditsDir = new File(musicFolder, "Credits");

			if (!creditsDir.exists())
			{
				creditsDir.mkdir();
			}

			if (DEBUG)
			{
				System.out.println("[Music Choices] Generating the \'Credits\' music folder.");
			}

			File achievementDir = new File(musicFolder, "Achievements");

			if (!achievementDir.exists())
			{
				achievementDir.mkdir();
			}

			if (DEBUG)
			{
				System.out.println("[Music Choices] Generating the \'Achievements\' music folder.");
			}

			if (DEBUG)
			{
				System.out.print("[Music Choices]  * In \'Achievements\' folder, generating folders:");
			}

			File specificAchievementDir;

			for (Object ach : AchievementList.achievementList)
			{
				if (ach != null && ach instanceof Achievement)
				{
					Achievement achievement = (Achievement)ach;
					specificAchievementDir = new File(achievementDir, achievement.getName());

					if (!specificAchievementDir.exists())
					{
						specificAchievementDir.mkdir();
					}

					if (DEBUG)
					{
						System.out.print(" \'" + achievement.getName() + "\'");
					}
				}
			}

			if (DEBUG)
			{
				System.out.println();
			}

			File bossesDir = new File(musicFolder, "Bosses");

			if (!bossesDir.exists())
			{
				bossesDir.mkdir();
			}

			if (DEBUG)
			{
				System.out.println("[Music Choices] Generating the \'Bosses\' music folder.");
			}

			File battleDir = new File(bossesDir, "Battle");

			if (!battleDir.exists())
			{
				battleDir.mkdir();
			}

			if (DEBUG)
			{
				System.out.println("[Music Choices]  * Generating the \'Battle\' music folder inside of \'Bosses\'.");
			}

			File victoryDir = new File(bossesDir, "Victory");

			if (!victoryDir.exists())
			{
				victoryDir.mkdir();
			}

			if (DEBUG)
			{
				System.out.println("[Music Choices]  * Generating the \'Victory\' music folder inside of \'Bosses\'.");
			}

			if (DEBUG)
			{
				System.out.print("[Music Choices]  * In \'Bosses\' folder, generating folders:");
			}

			Set<Class> classSet = EntityList.classToStringMapping.keySet();
			for (Class entityClass : classSet)
			{
				//Attempt to construct and pull information from this entity, namely
				//whether it is a boss or not. Note that it is likely to throw an
				//exception, since entities aren't normally constructed like this
				try
				{
					Entity boss = (Entity)entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {null});

					if (boss instanceof IBossDisplayData)
					{
						File bossDir = new File(bossesDir, boss.getEntityName());

						if (!bossDir.exists())
						{
							bossDir.mkdir();
						}

						File bossVictoryDir = new File(bossDir, "Victory");

						if (!bossVictoryDir.exists())
						{
							bossVictoryDir.mkdir();
						}

						if (DEBUG)
						{
							System.out.print(" \'" + boss.getEntityName() + "\'");
						}
					}
				}
				catch (Exception e)
				{
					System.err.println("\n[Music Choices] Handled an exception while creating a boss folder for class " + entityClass + ". It shoudn't be a problem.");
					e.printStackTrace();
				}
			}

			if (DEBUG)
			{
				System.out.println();
			}

			File creativeDir = new File(musicFolder, "Creative");

			if (!creativeDir.exists())
			{
				creativeDir.mkdir();
			}

			generateLightingFolders(musicFolder);

			if (DEBUG)
			{
				System.out.println("[Music Choices] Generating the \'Lighting\' music folders.");
			}

			if (DEBUG)
			{
				System.out.println("[Music Choices] Generating the \'Creative\' music folder.");
			}

			File biomeDir = new File(musicFolder, "Biomes");

			if (!biomeDir.exists())
			{
				biomeDir.mkdir();
			}

			if (DEBUG)
			{
				System.out.println("[Music Choices] Generating the \'Biomes\' music folder.");
			}

			if (DEBUG)
			{
				System.out.print("[Music Choices]    * In \'Biomes\' folder, generating folders:");
			}

			BiomeGenBase[] biomes = BiomeGenBase.biomeList;
			int biomesSize = biomes.length;
			File biomeFile;

			for (int i = 0; i < biomesSize; ++i)
			{
				BiomeGenBase biome = biomes[i];

				if (biome != null)
				{
					biomeFile = new File(biomeDir, biome.biomeName);

					if (!biomeFile.exists())
					{
						biomeFile.mkdir();
					}
					
					generateLightingFolders(biomeFile);

					if (DEBUG)
					{
						System.out.print(" \'" + biome.biomeName + "\'");
					}
				}
			}

			if (DEBUG)
			{
				System.out.println(", and all the 'Lighting' folders.");
			}

			File biomeTypeDir = new File(musicFolder, "Biome Types");

			if (!biomeTypeDir.exists())
			{
				biomeTypeDir.mkdir();
			}

			if (DEBUG)
			{
				System.out.println("[Music Choices] Generating the \'Biome Types\' music folder.");
			}

			if (DEBUG)
			{
				System.out.print("[Music Choices]    * In \'Biome Types\' folder, generating folders:");
			}

			Type[] biomeTypes = Type.values();
			int biomeTypesSize = biomeTypes.length;

			for (int i = 0; i < biomeTypesSize; ++i)
			{
				Type type = biomeTypes[i];
				File biomeTypeFolder = new File(biomeTypeDir, type.name());

				if (!biomeTypeFolder.exists())
				{
					biomeTypeFolder.mkdir();
				}
				
				generateLightingFolders(biomeTypeFolder);

				if (DEBUG)
				{
					System.out.print(" \'" + type.name() + "\'");
				}
			}

			if (DEBUG)
			{
				System.out.println();
			}

			int dimensions = MinecraftServer.getServer().worldServers.length;

			if (DEBUG)
			{
				System.out.println("[Music Choices] Looking for dimensions. Found " + dimensions + " of them.");
			}

			for (int i = 0; i < dimensions; ++i)
			{
				int dimensionNum = MinecraftServer.getServer().worldServers[i].provider.dimensionId;
				File dimensionDirectory = new File(musicFolder, String.valueOf(dimensionNum));

				if (!dimensionDirectory.exists())
				{
					dimensionDirectory.mkdir();
				}

				if (DEBUG)
				{
					System.out.println("[Music Choices] Generating music folder for dimension \'" + String.valueOf(dimensionNum) + "\'");
				}

				creativeDir = new File(dimensionDirectory, "Creative");

				if (!creativeDir.exists())
				{
					creativeDir.mkdir();
				}
				
				generateLightingFolders(dimensionDirectory);

				if (DEBUG)
				{
					System.out.println("[Music Choices]  * Generating folder \'Creative\' in \'" + String.valueOf(dimensionNum) + "\'");
				}

				biomeDir = new File(dimensionDirectory, "Biomes");

				if (!biomeDir.exists())
				{
					biomeDir.mkdir();
				}

				if (DEBUG)
				{
					System.out.println("[Music Choices]  * Generating folder \'Biomes\' in \'" + String.valueOf(dimensionNum) + "\'");
				}

				if (DEBUG)
				{
					System.out.print("[Music Choices]  * In \'Biomes\' folder, generating folders:");
				}

				int biomeNum = BiomeGenBase.biomeList.length;
				File biomeMusicFolder;

				for (int j = 0; j < biomeNum; ++j)
				{
					BiomeGenBase type = BiomeGenBase.biomeList[j];

					if (type != null)
					{
						biomeMusicFolder = new File(biomeDir, type.biomeName);

						if (!biomeMusicFolder.exists())
						{
							biomeMusicFolder.mkdir();
						}
						
						generateLightingFolders(biomeMusicFolder);

						if (DEBUG)
						{
							System.out.print(" \'" + type.biomeName + "\'");
						}
					}
				}

				if (DEBUG)
				{
					System.out.println();
				}

				biomeTypeDir = new File(dimensionDirectory, "Biome Types");

				if (!biomeTypeDir.exists())
				{
					biomeTypeDir.mkdir();
				}

				if (DEBUG)
				{
					System.out.println("[Music Choices]  * Generating folder \'Biome Types\' in \'" + String.valueOf(dimensionNum) + "\' folder.");
				}

				if (DEBUG)
				{
					System.out.print("[Music Choices]  * In \'Biome Types\' folder, generating folders:");
				}

				int typeNum = Type.values().length;

				for (int j = 0; j < typeNum; ++j)
				{
					Type type = Type.values()[j];
					biomeMusicFolder = new File(biomeTypeDir, type.name());

					if (!biomeMusicFolder.exists())
					{
						biomeMusicFolder.mkdir();
					}
					
					generateLightingFolders(biomeMusicFolder);

					if (DEBUG)
					{
						System.out.print(" \'" + type.name() + "\'");
					}
				}

				if (DEBUG)
				{
					System.out.println();
				}
			}
		}
		catch (Exception e)
		{
			System.err.println("Error while creating one or more dimension-specific music folders.");
			e.printStackTrace();
		}
	}

	private void generateLightingFolders(File parentDir)
	{
		File lightingDir = new File(parentDir, "Lighting");

		if (!lightingDir.exists())
		{
			lightingDir.mkdir();
		}

		File dayDir = new File(lightingDir, "Day");

		if (!dayDir.exists())
		{
			dayDir.mkdir();
		}

		File nightDir = new File(lightingDir, "Night");

		if (!nightDir.exists())
		{
			nightDir.mkdir();
		}

		File caveDir = new File(lightingDir, "Cave");

		if (!caveDir.exists())
		{
			caveDir.mkdir();
		}

		File artificialDir = new File(lightingDir, "Artificial");

		if (!artificialDir.exists())
		{
			artificialDir.mkdir();
		}

	}

	public void tickStart(EnumSet<TickType> type, Object ... tickData)
	{
		Minecraft mc = Minecraft.getMinecraft();
		SoundManager sm = mc.sndManager;
		SoundPoolEntry music;

		if (mc.theWorld == null)
		{
			
			if (sm != null && sm.sndSystem != null && mc.gameSettings.musicVolume != 0.0F && !sm.sndSystem.playing("BgMusic") && !sm.sndSystem.playing("streaming"))
			{
				if (this.ticksBeforeMenuMusic > 0)
				{
					--this.ticksBeforeMenuMusic;
				}
				else
				{
					music = MusicEventHandler.getMenuMusic();

					if (music != null)
					{
						if (DEBUG)
						{
							System.out.println("[Music Choices] Attempting to play menu music \'" + music.getSoundUrl() + "\' at volume " + mc.gameSettings.musicVolume);
						}

						this.ticksBeforeMenuMusic = MENU_MUSIC_INTERVAL;
						sm.sndSystem.backgroundMusic("BgMusic", music.getSoundUrl(), music.getSoundName(), false);
						sm.sndSystem.setVolume("BgMusic", mc.gameSettings.musicVolume);
						sm.sndSystem.play("BgMusic");
					}
				}
			}
		}
		else
		{
			--this.bossMusicTickdown;

			if (this.bossMusicTickdown <= 0 && sm.sndSystem != null)
			{
				this.bossMusicTickdown = 6;

				if (IMMEDIATE_BOSS_MUSIC && BossStatus.bossName != null && BossStatus.statusBarLength > 0 && !MusicEventHandler.bossMusicPlaying && !MusicEventHandler.victoryMusicPlaying)
				{
					music = MusicEventHandler.getBossMusic();

					if (music != null)
					{
						sm.sndSystem.stop("BgMusic");
						sm.sndSystem.backgroundMusic("BgMusic", music.getSoundUrl(), music.getSoundName(), false);
						sm.sndSystem.setVolume("BgMusic", mc.gameSettings.musicVolume);
						sm.sndSystem.play("BgMusic");
					}
				}

				if (PLAY_VICTORY_MUSIC && BossStatus.bossName != null && BossStatus.healthScale <= 0.0F && BossStatus.statusBarLength <= 0 && !MusicEventHandler.victoryMusicPlaying)
				{
					music = MusicEventHandler.getVictoryMusic();

					if (music != null)
					{
						sm.sndSystem.stop("BgMusic");
						sm.sndSystem.backgroundMusic("BgMusic", music.getSoundUrl(), music.getSoundName(), false);
						sm.sndSystem.setVolume("BgMusic", mc.gameSettings.musicVolume);
						sm.sndSystem.play("BgMusic");
					}

					MusicEventHandler.bossMusicPlaying = false;
					MusicEventHandler.victoryMusicPlaying = true;
					BossStatus.bossName = null;
				}
			}

			if (this.bossMusicTickdown == 2)
			{
				if (MusicEventHandler.bossMusicPlaying)
				{
					if (!sm.sndSystem.playing("BgMusic"))
					{
						MusicEventHandler.bossMusicPlaying = false;
					}

					if (CANCEL_BOSS_MUSIC && BossStatus.statusBarLength <= 0)
					{
						sm.sndSystem.stop("BgMusic");
						MusicEventHandler.bossMusicPlaying = false;
					}
				}

				if (MusicEventHandler.victoryMusicPlaying && !sm.sndSystem.playing("BgMusic"))
				{
					MusicEventHandler.victoryMusicPlaying = false;
				}
			}

			if (MusicEventHandler.creditsMusicPlaying)
			{
				if (!sm.sndSystem.playing("BgMusic"))
				{
					MusicEventHandler.creditsMusicPlaying = false;
				}

				if (!(mc.currentScreen instanceof GuiWinGame))
				{
					sm.sndSystem.stop("BgMusic");
					MusicEventHandler.creditsMusicPlaying = false;
				}
			}

			--this.achievementMusicTickdown;

			if (this.achievementMusicTickdown <= 0)
			{
				this.achievementMusicTickdown = 10;

				for (Object a : AchievementList.achievementList)
				{
					Achievement ach = (Achievement)a;

					if (mc.statFileWriter.hasAchievementUnlocked(ach) && !((Boolean)this.achievementsUnlocked.get(ach)).booleanValue())
					{
						this.achievementsUnlocked.put(ach, Boolean.valueOf(true));
						SoundPoolEntry soundpoolentry = MusicEventHandler.getAchievementMusic(ach);

						if (soundpoolentry != null)
						{
							String musicType = LET_ACH_MUSIC_OVERLAP ? "AchMusic" : "BgMusic";
							sm.sndSystem.stop(musicType);
							sm.sndSystem.backgroundMusic(musicType, soundpoolentry.getSoundUrl(), soundpoolentry.getSoundName(), false);
							sm.sndSystem.setVolume(musicType, mc.gameSettings.musicVolume);
							sm.sndSystem.play(musicType);
						}
					}
				}
			}
		}
	}

	public void tickEnd(EnumSet<TickType> type, Object ... tickData)
	{
		Minecraft mc = Minecraft.getMinecraft();

		if (mc.theWorld != null && mc.thePlayer != null && mc.theWorld.getWorldInfo() != null)
		{
			if (mc.theWorld.getWorldInfo().getWorldName() != worldName)
			{
				this.loadMusic();
				worldName = mc.theWorld.getWorldInfo().getWorldName();

				if (DEBUG)
				{
					System.out.println("[Music Choices] Setting world name as \'" + worldName + "\'");
				}
			}

			for (Object a : AchievementList.achievementList)
			{
				if (a instanceof Achievement && a != null)
				{
					Achievement ach = (Achievement)a;
					this.achievementsUnlocked.put(ach, Boolean.valueOf(mc.statFileWriter.hasAchievementUnlocked(ach)));
				}
			}
		}
		else if (worldName != null)
		{
			worldName = null;

			if (DEBUG)
			{
				System.out.println("[Music Choices] Setting world name as null");
			}

			if (mc.sndManager != null && mc.sndManager.sndSystem != null)
			{
				mc.sndManager.sndSystem.stop("BgMusic");

				if (DEBUG)
				{
					System.out.println("[Music Choices] Stopping background music.");
				}
			}

			this.achievementsLoaded = false;
		}
	}

	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.CLIENT);
	}

	public String getLabel()
	{
		return "Dimension Music Tick Handler";
	}
}
