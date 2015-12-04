package mezz.jei;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.network.NetHandlerPlayClient;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIManager;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeTransferHelper;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.network.packets.PacketJEI;
import mezz.jei.util.Log;

public class ProxyCommonClient extends ProxyCommon {
	private ItemFilter itemFilter;
	private GuiEventHandler guiEventHandler;

	@Override
	public void preInit(@Nonnull FMLPreInitializationEvent event) {
		Config.preInit(event);
	}

	@Override
	public void init(@Nonnull FMLInitializationEvent event) {
		KeyBindings.init();
		FMLCommonHandler.instance().bus().register(this);

		guiEventHandler = new GuiEventHandler();
		MinecraftForge.EVENT_BUS.register(guiEventHandler);
		FMLCommonHandler.instance().bus().register(guiEventHandler);
	}

	@Override
	public void startJEI(@Nonnull Set<ASMDataTable.ASMData> modPlugins) {
		JEIManager.itemRegistry = new ItemRegistry();
		JEIManager.recipeRegistry = createRecipeRegistry(modPlugins);

		itemFilter = new ItemFilter();
		ItemListOverlay itemListOverlay = new ItemListOverlay(itemFilter);
		guiEventHandler.setItemListOverlay(itemListOverlay);

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

	private static RecipeRegistry createRecipeRegistry(@Nonnull Set<ASMDataTable.ASMData> modPluginsData) {
		List<IModPlugin> plugins = new ArrayList<>();
		for (ASMDataTable.ASMData asmData : modPluginsData) {
			try {
				Class<?> asmClass = Class.forName(asmData.getClassName());
				Class<? extends IModPlugin> modPluginClass = asmClass.asSubclass(IModPlugin.class);
				IModPlugin plugin = modPluginClass.newInstance();
				if (plugin.isModLoaded()) {
					plugins.add(plugin);
				}
			} catch (Throwable e) {
				FMLLog.bigWarning("Failed to load mod plugin: {}", asmData.getClassName());
				Log.error("Exception: {}", e);
			}
		}

		ImmutableList.Builder<IRecipeCategory> recipeCategories = ImmutableList.builder();
		ImmutableList.Builder<IRecipeHandler> recipeHandlers = ImmutableList.builder();
		ImmutableList.Builder<IRecipeTransferHelper> recipeTransferHelpers = ImmutableList.builder();
		ImmutableList.Builder<Object> recipes = ImmutableList.builder();

		for (IModPlugin plugin : plugins) {
			recipeCategories.addAll(plugin.getRecipeCategories());
			recipeHandlers.addAll(plugin.getRecipeHandlers());
			recipeTransferHelpers.addAll(plugin.getRecipeTransferHelpers());
			recipes.addAll(plugin.getRecipes());
		}

		return new RecipeRegistry(recipeCategories.build(), recipeHandlers.build(), recipeTransferHelpers.build(), recipes.build());
	}
}
