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
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class JustEnoughItems implements ModInitializer {
	@Override
	public void onInitialize() {
		IServerConfig serverConfig = ServerConfig.getInstance();
		IConnectionToClient connection = new ConnectionToClient();
		ServerNetworkHandler.registerServerPacketHandlers(connection, serverConfig);

		ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "jei_shaped");
		var recipeSerializer = new JeiShapedRecipe.Serializer();
		var registered = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, resourceLocation, recipeSerializer);
		RecipeSerializers.register(() -> registered);

		// Run through vanilla recipe serializaers and sync them
		for (var serializer : BuiltInRegistries.RECIPE_SERIALIZER.keySet()) {
			if (serializer.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
				RecipeSynchronization.synchronizeRecipeSerializer(Objects.requireNonNull(BuiltInRegistries.RECIPE_SERIALIZER.getValue(serializer)));
			}
		}
	}
}
