package com.tmtravlr.musicchoices;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundPoolEntry;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.stats.Achievement;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.sound.PlayBackgroundMusicEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.ForgeSubscribe;
import paulscode.sound.SoundSystem;

@SideOnly(Side.CLIENT)
public class MusicEventHandler
{
    private static Random rand = new Random();
    public static boolean bossMusicPlaying = false;
    public static boolean victoryMusicPlaying = false;
    public static boolean creditsMusicPlaying = false;

    /**
     * Changes the background music, picking a random music file out of all
     * that apply in that situation (so in that dimension, in that biome, etc.).
     * If there are no music files that apply, it lets the default music
     * play.
     */
    @ForgeSubscribe
    public void onMusic(PlayBackgroundMusicEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        int dimension = mc.theWorld.provider.dimensionId;
        BiomeGenBase biome = mc.theWorld.getBiomeGenForCoords(MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.posZ));
        String biomeName = biome.biomeName;
        boolean isCreative = mc.thePlayer.capabilities.isCreativeMode;

        if (MusicChoicesMod.DEBUG)
        {
            System.out.println("muc -- The music track \'" + event.result.getSoundUrl() + "\' is attempting to play.");
        }

        try
        {
            if (MusicChoicesMod.DEBUG)
            {
                System.out.println("muc -- Searching through music folders for music tracks to play.");
            }

            File musicFolder = new File(MusicChoicesMod.musicFolderName);

            if (!musicFolder.exists())
            {
                musicFolder.mkdir();
            }

            ArrayList<File> directories = new ArrayList();
            boolean bossPresent = false;
            File folder;

            if (BossStatus.bossName != null && BossStatus.statusBarLength > 0)
            {
                folder = new File(musicFolder, "Bosses");

                if (!folder.exists())
                {
                    folder.mkdir();
                }

                directories.add(folder);
                directories.add(new File(folder, BossStatus.bossName));
                bossPresent = true;
            }

            File biomesFolder;
            File dimensionBiomesFolder;

            if (!bossPresent || !MusicChoicesMod.ONLY_BOSS_MUSIC_FOR_BOSSES)
            {
                folder = new File(musicFolder, String.valueOf(dimension));
                directories.add(folder);

                if (isCreative)
                {
                    directories.add(new File(musicFolder, "Creative"));
                    directories.add(new File(folder, "Creative"));
                }

                biomesFolder = new File(musicFolder, "Biomes");
                dimensionBiomesFolder = new File(folder, "Biomes");

                if (!biomesFolder.exists())
                {
                    biomesFolder.mkdir();
                }

                if (!dimensionBiomesFolder.exists())
                {
                    dimensionBiomesFolder.mkdir();
                }

                directories.add(new File(biomesFolder, biomeName));
                directories.add(new File(dimensionBiomesFolder, biomeName));
                
                File biomeTypesFolder = new File(musicFolder, "Biome Types");
                File dimensionBiomeTypesFolder = new File(folder, "Biome Types");

                if (!biomeTypesFolder.exists())
                {
                    biomeTypesFolder.mkdir();
                }

                if (!dimensionBiomeTypesFolder.exists())
                {
                    dimensionBiomeTypesFolder.mkdir();
                }

                for (int i = 0; i < BiomeDictionary.getTypesForBiome(biome).length; ++i)
                {
                    Type biomeType = BiomeDictionary.getTypesForBiome(biome)[i];
                    directories.add(new File(biomeTypesFolder, biomeType.name()));
                    directories.add(new File(dimensionBiomeTypesFolder, biomeType.name()));
                }
            }

            //Look through all the applicable music directories, and add all
            //the music files to a list
            ArrayList<File> musicToChooseFrom = new ArrayList();
            for(File directory : directories)
            {
                if (MusicChoicesMod.DEBUG)
                {
                    System.out.println(" * muc -- Looking in directory \'" + directory.getPath() + "\'");
                }

                if (!directory.exists())
                {
                    boolean successful = directory.mkdir();

                    if (MusicChoicesMod.DEBUG)
                    {
                        System.out.println("   * muc -- \'" + directory.getPath() + "\' didn\'t exist yet. Attempting to create it. Successful? " + successful);
                    }
                }

                if (directory.isDirectory() && directory.listFiles().length > 0)
                {
                    for (int i = 0; i < directory.listFiles().length; ++i)
                    {
                        File musicFile = directory.listFiles()[i];

                        if (musicFile != null && musicFile.exists() && musicFile.getName().substring(musicFile.getName().lastIndexOf(46) + 1).equals("ogg"))
                        {
                            musicToChooseFrom.add(musicFile);

                            if (MusicChoicesMod.DEBUG)
                            {
                                System.out.println("   * muc -- Found a .ogg file! Adding the file \'" + musicFile.getName() + "\' to list of music to choose from.");
                            }
                        }
                    }
                }
            }

            //Choose a random music file from the list of applicable files
            if (musicToChooseFrom.size() > 0)
            {
                File musicChoice = (File)musicToChooseFrom.get(rand.nextInt(musicToChooseFrom.size()));
                event.result = new SoundPoolEntry(musicChoice.getName(), musicChoice.toURI().toURL());

                if (MusicChoicesMod.DEBUG)
                {
                    System.out.println("muc -- Found " + musicToChooseFrom.size() + " music file" + (musicToChooseFrom.size() == 1 ? "" : "s") + ". Changed music playing to \'" + musicChoice.getName() + "\'");
                }
            }
            else if (MusicChoicesMod.DEBUG)
            {
                System.out.println("muc -- Didn\'t find any music to play in this circumstance. Playing default music.");
            }
        }
        catch (Exception e)
        {
            System.err.println("muc -- Error while playing custom music! =(");
            e.printStackTrace();
        }
    }

    /**
     * Returns a music entry to play in the menu
     */
    public static SoundPoolEntry getMenuMusic()
    {
        if (MusicChoicesMod.DEBUG)
        {
            System.out.println("muc -- Attempting to play a Menu music track.");
        }

        try
        {
            if (MusicChoicesMod.DEBUG)
            {
                System.out.println("muc -- Looking in the \'Menu\' folder for music to play.");
            }

            File musicFolder = new File(MusicChoicesMod.musicFolderName);

            if (!musicFolder.exists())
            {
                musicFolder.mkdir();
            }

            File menuMusicDirectory = new File(musicFolder, "Menu");

            if (!menuMusicDirectory.exists())
            {
                menuMusicDirectory.mkdir();
            }

            File[] musicFiles = menuMusicDirectory.listFiles();

            //Pick a random file in the Menu folder, if there are any
            if (musicFiles != null && musicFiles.length > 0)
            {
                File musicToPlay = musicFiles[rand.nextInt(musicFiles.length)];

                if (!musicToPlay.getName().substring(musicToPlay.getName().lastIndexOf(46) + 1).equals("ogg"))
                {
                    if (MusicChoicesMod.DEBUG)
                    {
                        System.out.println("muc -- The selected music track wasn\'t a .ogg file! Only .ogg files can play.");
                    }

                    return null;
                }

                if (MusicChoicesMod.DEBUG)
                {
                    System.out.println("muc -- Playing Menu music track \'" + musicToPlay.getName() + "\'");
                }

                return new SoundPoolEntry(musicToPlay.getName(), musicToPlay.toURI().toURL());
            }

            if (MusicChoicesMod.DEBUG)
            {
                System.out.println("muc -- Couldn\'t find any Menu music to play.");
            }
        }
        catch (Exception e)
        {
            System.err.println("muc -- Error while playing custom menu music! =(");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns a music entry for the boss whose health bar is currently displayed
     */
    public static SoundPoolEntry getBossMusic()
    {
        if (BossStatus.bossName != null && BossStatus.statusBarLength > 0)
        {
            try
            {
                if (MusicChoicesMod.DEBUG)
                {
                    System.out.println("muc -- Attempting to play immediate boss music.");
                }

                File musicFolder = new File(MusicChoicesMod.musicFolderName);

                if (!musicFolder.exists())
                {
                    musicFolder.mkdir();
                }

                File bossesDir = new File(musicFolder, "Bosses");

                if (!bossesDir.exists())
                {
                    bossesDir.mkdir();
                }

                File specificBossDir = new File(bossesDir, BossStatus.bossName);

                if (!specificBossDir.exists())
                {
                    specificBossDir.mkdir();
                }

                ArrayList musicToChooseFrom = new ArrayList();
                int i;
                File file;

                //Add all files for the given boss and for bosses in general
                for (i = 0; i < bossesDir.listFiles().length; ++i)
                {
                    file = bossesDir.listFiles()[i];

                    if (file != null && file.exists() && file.getName().substring(file.getName().lastIndexOf(46) + 1).equals("ogg"))
                    {
                        musicToChooseFrom.add(file);

                        if (MusicChoicesMod.DEBUG)
                        {
                            System.out.println("Found a .ogg file! Adding the file \'" + file.getName() + "\' to list of music to choose from.");
                        }
                    }
                }

                for (i = 0; i < specificBossDir.listFiles().length; ++i)
                {
                    file = specificBossDir.listFiles()[i];

                    if (file != null && file.exists() && file.getName().substring(file.getName().lastIndexOf(46) + 1).equals("ogg"))
                    {
                        musicToChooseFrom.add(file);

                        if (MusicChoicesMod.DEBUG)
                        {
                            System.out.println("Found a .ogg file! Adding the file \'" + file.getName() + "\' to list of music to choose from.");
                        }
                    }
                }

                //Choose a random file to play
                if (musicToChooseFrom.size() > 0)
                {
                    File musicChoice = (File)musicToChooseFrom.get(rand.nextInt(musicToChooseFrom.size()));

                    if (MusicChoicesMod.DEBUG)
                    {
                        System.out.println("muc -- Found " + musicToChooseFrom.size() + " music file" + (musicToChooseFrom.size() == 1 ? "" : "s") + ". Playing boss music \'" + musicChoice.getName() + "\'");
                    }

                    bossMusicPlaying = true;
                    return new SoundPoolEntry(musicChoice.getName(), musicChoice.toURI().toURL());
                }

                if (MusicChoicesMod.DEBUG)
                {
                    System.out.println("muc -- Didn\'t find any immediate boss music to play for this boss: \'" + BossStatus.bossName + "\'.");
                }
            }
            catch (Exception e)
            {
                System.err.println("muc -- Error while playing custom boss music! =(");
                e.printStackTrace();
            }

            return null;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns a music entry for a "victory jingle" to play for the current boss
     * (whose health bar is currently displayed)
     */
    public static SoundPoolEntry getVictoryMusic()
    {
        if (BossStatus.bossName == null)
        {
            return null;
        }
        else
        {
            try
            {
                if (MusicChoicesMod.DEBUG)
                {
                    System.out.println("muc -- Attempting to play boss victory music.");
                }

                File musicFolder = new File(MusicChoicesMod.musicFolderName);

                if (!musicFolder.exists())
                {
                    musicFolder.mkdir();
                }

                File bossesDir = new File(musicFolder, "Bosses");

                if (!bossesDir.exists())
                {
                    bossesDir.mkdir();
                }

                File victoryDir = new File(bossesDir, "Victory");

                if (!victoryDir.exists())
                {
                    victoryDir.mkdir();
                }

                File specificBossDir = new File(bossesDir, BossStatus.bossName);

                if (!specificBossDir.exists())
                {
                    specificBossDir.mkdir();
                }

                File specificVictoryDir = new File(specificBossDir, "Victory");

                if (!specificVictoryDir.exists())
                {
                    specificVictoryDir.mkdir();
                }

                ArrayList musicToChooseFrom = new ArrayList();
                int i;
                File file;

                //Same as with bosses, but look in the Victory sub-folders 
                for (i = 0; i < victoryDir.listFiles().length; ++i)
                {
                    file = victoryDir.listFiles()[i];

                    if (file != null && file.exists() && file.getName().substring(file.getName().lastIndexOf(46) + 1).equals("ogg"))
                    {
                        musicToChooseFrom.add(file);

                        if (MusicChoicesMod.DEBUG)
                        {
                            System.out.println("Found a .ogg file! Adding the file \'" + file.getName() + "\' to list of music to choose from.");
                        }
                    }
                }

                for (i = 0; i < specificVictoryDir.listFiles().length; ++i)
                {
                    file = specificVictoryDir.listFiles()[i];

                    if (file != null && file.exists() && file.getName().substring(file.getName().lastIndexOf(46) + 1).equals("ogg"))
                    {
                        musicToChooseFrom.add(file);

                        if (MusicChoicesMod.DEBUG)
                        {
                            System.out.println("Found a .ogg file! Adding the file \'" + file.getName() + "\' to list of music to choose from.");
                        }
                    }
                }

                if (musicToChooseFrom.size() > 0)
                {
                    File musicChoice = (File)musicToChooseFrom.get(rand.nextInt(musicToChooseFrom.size()));

                    if (MusicChoicesMod.DEBUG)
                    {
                        System.out.println("muc -- Found " + musicToChooseFrom.size() + " music file" + (musicToChooseFrom.size() == 1 ? "" : "s") + ". Playing boss music \'" + musicChoice.getName() + "\'");
                    }

                    return new SoundPoolEntry(musicChoice.getName(), musicChoice.toURI().toURL());
                }

                if (MusicChoicesMod.DEBUG)
                {
                    System.out.println("muc -- Didn\'t find any boss victory music to play for this boss: \'" + BossStatus.bossName + "\'.");
                }
            }
            catch (Exception e)
            {
                System.err.println("muc -- Error while playing custom victory music! =(");
                e.printStackTrace();
            }

            return null;
        }
    }

    /**
     * Returns a music entry to play for the given achievement
     */
    public static SoundPoolEntry getAchievementMusic(Achievement a)
    {
        try
        {
            if (MusicChoicesMod.DEBUG)
            {
                System.out.println("muc -- Attempting to play achievement music.");
            }

            File musicFolder = new File(MusicChoicesMod.musicFolderName);

            if (!musicFolder.exists())
            {
                musicFolder.mkdir();
            }

            File achievementDir = new File(musicFolder, "Achievements");

            if (!achievementDir.exists())
            {
                achievementDir.mkdir();
            }

            File specificAchFolder = new File(achievementDir, a.getName());

            if (!specificAchFolder.exists())
            {
                specificAchFolder.mkdir();
            }

            //Add all music files that could play for the given achievement to a list
            ArrayList musicToChooseFrom = new ArrayList();
            int i;
            File file;

            for (i = 0; i < achievementDir.listFiles().length; ++i)
            {
                file = achievementDir.listFiles()[i];

                if (file != null && file.exists() && file.getName().substring(file.getName().lastIndexOf(46) + 1).equals("ogg"))
                {
                    musicToChooseFrom.add(file);

                    if (MusicChoicesMod.DEBUG)
                    {
                        System.out.println("Found a .ogg file! Adding the file \'" + file.getName() + "\' to list of music to choose from.");
                    }
                }
            }

            for (i = 0; i < specificAchFolder.listFiles().length; ++i)
            {
                file = specificAchFolder.listFiles()[i];

                if (file != null && file.exists() && file.getName().substring(file.getName().lastIndexOf(46) + 1).equals("ogg"))
                {
                    musicToChooseFrom.add(file);

                    if (MusicChoicesMod.DEBUG)
                    {
                        System.out.println("Found a .ogg file! Adding the file \'" + file.getName() + "\' to list of music to choose from.");
                    }
                }
            }

            if (musicToChooseFrom.size() > 0)
            {
                File var10 = (File)musicToChooseFrom.get(rand.nextInt(musicToChooseFrom.size()));

                if (MusicChoicesMod.DEBUG)
                {
                    System.out.println("muc -- Found " + musicToChooseFrom.size() + " music file" + (musicToChooseFrom.size() == 1 ? "" : "s") + ". Playing achievement music \'" + var10.getName() + "\'");
                }

                return new SoundPoolEntry(var10.getName(), var10.toURI().toURL());
            }

            if (MusicChoicesMod.DEBUG)
            {
                System.out.println("muc -- Didn\'t find any achievement music to play for the achievement \'" + a.getName() + "\'.");
            }
        }
        catch (Exception var9)
        {
            System.err.println("muc -- Error while playing custom victory music! =(");
            var9.printStackTrace();
        }

        return null;
    }

    /**
     * Plays the credits music when the credits GUI is opened.
     */
    @ForgeSubscribe
    public void onCredits(GuiOpenEvent event)
    {
        if (event.gui instanceof GuiWinGame)
        {
            try
            {
                if (MusicChoicesMod.DEBUG)
                {
                    System.out.println("muc -- Attempting to play boss victory music.");
                }

                File musicFolder = new File(MusicChoicesMod.musicFolderName);

                if (!musicFolder.exists())
                {
                    musicFolder.mkdir();
                }

                File creditsDir = new File(musicFolder, "Credits");

                if (!creditsDir.exists())
                {
                    creditsDir.mkdir();
                }

                //Add all possible music files for credits to a list
                ArrayList<File> musicToChooseFrom = new ArrayList();
                File[] musicToPlay = creditsDir.listFiles();
                int toPlay = musicToPlay.length;

                for (int i = 0; i < toPlay; ++i)
                {
                    File file = musicToPlay[i];

                    if (file != null && file.exists() && file.getName().substring(file.getName().lastIndexOf(46) + 1).equals("ogg"))
                    {
                        musicToChooseFrom.add(file);

                        if (MusicChoicesMod.DEBUG)
                        {
                            System.out.println("muc -- Found a .ogg file! Adding the file \'" + file.getName() + "\' to list of music to choose from.");
                        }
                    }
                }

                //Choose a random file from that list
                if (musicToChooseFrom.size() > 0)
                {
                    File musicChoice = musicToChooseFrom.get(rand.nextInt(musicToChooseFrom.size()));

                    if (MusicChoicesMod.DEBUG)
                    {
                        System.out.println("muc -- Found " + musicToChooseFrom.size() + " music file" + (musicToChooseFrom.size() == 1 ? "" : "s") + ". Playing boss music \'" + musicChoice.getName() + "\'");
                    }

                    SoundPoolEntry music = new SoundPoolEntry(musicChoice.getName(), musicChoice.toURI().toURL());
                    SoundSystem ss = Minecraft.getMinecraft().sndManager.sndSystem;

                    if (ss != null)
                    {
                        ss.stop("BgMusic");
                        ss.backgroundMusic("BgMusic", music.getSoundUrl(), music.getSoundName(), false);
                        ss.setVolume("BgMusic", Minecraft.getMinecraft().gameSettings.musicVolume);
                        ss.play("BgMusic");
                        creditsMusicPlaying = true;
                    }
                }
                else if (MusicChoicesMod.DEBUG)
                {
                    System.out.println("muc -- Didn\'t find any boss victory music to play for this boss: \'" + BossStatus.bossName + "\'.");
                }
            }
            catch (Exception e)
            {
                System.err.println("muc -- Error while playing credits music! =(");
                e.printStackTrace();
            }
        }
    }
}
