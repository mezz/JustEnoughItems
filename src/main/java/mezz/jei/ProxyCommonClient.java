package mezz.jei;

import javax.annotation.Nonnull;

import net.minecraft.client.network.NetHandlerPlayClient;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import mezz.jei.api.JEIManager;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.network.packets.PacketJEI;
import mezz.jei.plugins.vanilla.VanillaPlugin;

public class ProxyCommonClient extends ProxyCommon {
	private ItemFilter itemFilter;

	@Override
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		Config.preInit(event);
		JEIManager.pluginRegistry.registerPlugin(new VanillaPlugin());
	}

	@Override
	public void init(@Nonnull FMLInitializationEvent event) {
		KeyBindings.init();
		FMLCommonHandler.instance().bus().register(this);
	}

	@Override
	public void loadComplete(@Nonnull FMLLoadCompleteEvent event) {
		JEIManager.itemRegistry = new ItemRegistry();
		JEIManager.recipeRegistry = JustEnoughItems.pluginRegistry.createRecipeRegistry();

		itemFilter = new ItemFilter();
		ItemListOverlay itemListOverlay = new ItemListOverlay(itemFilter);
		GuiEventHandler guiEventHandler = new GuiEventHandler(itemListOverlay);
		MinecraftForge.EVENT_BUS.register(guiEventHandler);
		FMLCommonHandler.instance().bus().register(guiEventHandler);

		TooltipEventHandler tooltipEventHandler = new TooltipEventHandler();
		MinecraftForge.EVENT_BUS.register(tooltipEventHandler);
	}

	@Override
	public void sendPacketToServer(PacketJEI packet) {
		NetHandlerPlayClient netHandler = FMLClientHandler.instance().getClient().getNetHandler();
		if (netHandler != null) {
			netHandler.addToSendQueue(packet.getPacket());
		}
	}

	@SubscribeEvent
	public void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(Constants.MOD_ID)) {
			Config.syncConfig();
			itemFilter.reset();
		}
	}
}
