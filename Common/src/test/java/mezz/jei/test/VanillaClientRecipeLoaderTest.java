package mezz.jei.test;

import mezz.jei.common.recipes.VanillaClientRecipeLoader;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests loading vanilla recipes from the client-bundled server-data resources.
 */
public class VanillaClientRecipeLoaderTest {
	@Test
	public void loadsVanillaRecipesFromClientResources() {
		// Setup
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
		RegistryAccess registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

		// Operation
		RecipeMap recipeMap = VanillaClientRecipeLoader.getVanillaRecipes(registryAccess);

		// Assertions
		assertFalse(recipeMap.values().isEmpty(), "Expected vanilla recipes to load from client resources.");
		ResourceKey<Recipe<?>> craftingTableKey = ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("crafting_table"));
		assertNotNull(recipeMap.byKey(craftingTableKey), "Expected the vanilla crafting table recipe to be loaded.");
	}
}
