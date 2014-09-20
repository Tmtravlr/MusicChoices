package com.tmtravlr.musicchoices;

import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy
{
    public void registerEventHandlers()
    {
        MinecraftForge.EVENT_BUS.register(new MusicEventHandler());
    }
}
