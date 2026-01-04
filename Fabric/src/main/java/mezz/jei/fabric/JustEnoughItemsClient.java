package mezz.jei.fabric;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.JeiAtlasManager;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.MinecraftLocaleSupplier;
import mezz.jei.common.util.Translator;
import mezz.jei.fabric.events.JeiLifecycleEvents;
import mezz.jei.fabric.plugins.fabric.FabricGuiPlugin;
import mezz.jei.fabric.startup.ClientLifecycleHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.RecipeMap;

@SuppressWarnings("unused")
public class JustEnoughItemsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Translator.setLocaleSupplier(new MinecraftLocaleSupplier());
		ClientLifecycleHandler clientLifecycleHandler = new ClientLifecycleHandler();

		ClientRecipeSynchronizedEvent.EVENT.register((minecraft, synchronizedRecipes) -> {
			Internal.setClientSyncedRecipes(RecipeMap.create(synchronizedRecipes.recipes()));
			JeiLifecycleEvents.AFTER_RECIPE_SYNC.invoker().run();
		});

		JeiLifecycleEvents.REGISTER_RESOURCE_RELOAD_LISTENER.register((resourceManager, textureManager) -> {
			Textures textures = Internal.getTextures();
			JeiAtlasManager atlasManager = textures.getAtlasManager();
			resourceManager.registerReloadListener(atlasManager);

			ClientLifecycleEvents.CLIENT_STARTED.register(event -> {
				clientLifecycleHandler.registerEvents();

				ResourceLoader.get(PackType.SERVER_DATA)
					.registerReloadListener(Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "lifecycle"), clientLifecycleHandler.getReloadListener());

				ResourceLoader.get(PackType.CLIENT_RESOURCES)
					.registerReloadListener(Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "resources_reload"), createReloadListener());
			});
		});
	}

	public ResourceManagerReloadListener createReloadListener() {
		return (resourceManager) -> {
			FabricGuiPlugin.getResourceReloadHandler()
				.ifPresent(r -> r.onResourceManagerReload(resourceManager));
		};
	}
}
