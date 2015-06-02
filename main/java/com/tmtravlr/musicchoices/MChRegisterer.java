package com.tmtravlr.musicchoices;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.tmtravlr.musicchoices.musicloader.MusicResourceReloadListener;

public class MChRegisterer {

	public static void registerEventHandlers() {
		MinecraftForge.EVENT_BUS.register(new MusicChoicesEventHandler());
	}
	
	public static void registerTickHandlers() {
		FMLCommonHandler.instance().bus().register(new MusicChoicesTickHandler());
	}
	
	public static void registerResourceReloadListeners() {
		IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
		
		if(manager != null && manager instanceof SimpleReloadableResourceManager) {
			SimpleReloadableResourceManager simpleManager = (SimpleReloadableResourceManager) manager;
			
			simpleManager.registerReloadListener(new MusicResourceReloadListener());
		}
	}
	
}
