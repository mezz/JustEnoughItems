package mezz.jei.test;

import mezz.jei.common.recipes.VanillaClientRecipeLoader;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.crafting.Recipe;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests loading vanilla recipes from the client-bundled server-data resources.
 */
public class VanillaClientRecipeLoaderTest {
	@Test
	public void loadsVanillaRecipesFromClientResources() {
		// Setup
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();

		// Operation
		List<Recipe<?>> recipes = VanillaClientRecipeLoader.getVanillaRecipes();

		// Assertions
		assertFalse(recipes.isEmpty(), "Expected vanilla recipes to load from client resources.");
		ResourceLocation craftingTableId = new ResourceLocation("minecraft", "crafting_table");
		assertTrue(
			recipes.stream().anyMatch(recipe -> recipe.getId().equals(craftingTableId)),
			"Expected the vanilla crafting table recipe to be loaded."
		);
	}
}
