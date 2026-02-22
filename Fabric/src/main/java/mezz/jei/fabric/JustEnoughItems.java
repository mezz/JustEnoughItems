package mezz.jei.fabric;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.IServerConfig;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.fabric.config.ServerConfig;
import mezz.jei.fabric.network.ConnectionToClient;
import mezz.jei.fabric.network.ServerNetworkHandler;
import mezz.jei.library.plugins.vanilla.crafting.JeiShapedRecipe;
import mezz.jei.library.recipes.RecipeSerializers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JustEnoughItems implements ModInitializer {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize() {
		IServerConfig serverConfig = ServerConfig.getInstance();
		IConnectionToClient connection = new ConnectionToClient();
		ServerNetworkHandler.registerServerPacketHandlers(connection, serverConfig);

		Identifier id = Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "jei_shaped");
		var registered = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, JeiShapedRecipe.SERIALIZER);
		RecipeSerializers.register(() -> registered);

		// Run through vanilla recipe serializers and sync them
		for (var entry : BuiltInRegistries.RECIPE_SERIALIZER.entrySet()) {
			ResourceKey<RecipeSerializer<?>> resourceKey = entry.getKey();
			if (resourceKey.identifier().getNamespace().equals(ModIds.MINECRAFT_ID)) {
				RecipeSerializer<?> serializer = entry.getValue();
				try {
					RecipeSynchronization.synchronizeRecipeSerializer(serializer);
				} catch (RuntimeException e) {
					LOGGER.warn("Failed to synchronize recipe serializer {}", resourceKey, e);
				}
			}
		}
	}
}
