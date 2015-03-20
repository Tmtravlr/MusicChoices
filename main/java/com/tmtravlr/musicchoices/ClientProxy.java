package com.tmtravlr.musicchoices;

import com.tmtravlr.musicchoices.musicloader.MusicResourceReloadListener;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;

/**
 * Client Proxy
 * 
 * @author Rebeca Rey
 * @Date Febuary 2015 
 */
public class ClientProxy extends CommonProxy {

	@Override
	public void registerEventHandlers() {
		MinecraftForge.EVENT_BUS.register(new MusicChoicesEventHandler());
	}
	
	@Override
	public void registerTickHandlers() {
		FMLCommonHandler.instance().bus().register(new MusicChoicesTickHandler());
	}
	
	@Override
	public void registerResourceReloadListeners() {
		IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
		
		if(manager != null && manager instanceof SimpleReloadableResourceManager) {
			SimpleReloadableResourceManager simpleManager = (SimpleReloadableResourceManager) manager;
			
			simpleManager.registerReloadListener(new MusicResourceReloadListener());
		}
	}
	
}
