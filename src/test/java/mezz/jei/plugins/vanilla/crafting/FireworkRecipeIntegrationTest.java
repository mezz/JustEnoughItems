package mezz.jei.plugins.vanilla.crafting;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeFireworks;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FireworkRecipeIntegrationTest {
	@BeforeClass
	public static void bootstrapMinecraft() {
		if (!Bootstrap.isRegistered()) {
			Bootstrap.register();
		}
	}

	@Test
	public void displayedFireworkRecipesCraftTheirDisplayedOutputs() {
		List<FireworkRecipeWrapper> recipes = FireworkRecipeMaker.getFireworkRecipes();
		assertTrue("Expected synthetic firework recipes", !recipes.isEmpty());

		for (FireworkRecipeWrapper recipe : recipes) {
			int variations = getVariationCount(recipe);
			for (int variation = 0; variation < variations; variation++) {
				assertRecipeCraftsOutput(recipe, variation, variations);
			}
		}
	}

	private static int getVariationCount(FireworkRecipeWrapper recipe) {
		int variations = recipe.getOutputs().size();
		for (List<ItemStack> input : recipe.getInputs()) {
			assertTrue("Firework recipe input must not be empty", !input.isEmpty());
		}
		return variations;
	}

	private static void assertRecipeCraftsOutput(FireworkRecipeWrapper recipe, int variation, int variations) {
		InventoryCrafting crafting = new InventoryCrafting(new TestContainer(), 3, 3);
		List<List<ItemStack>> inputs = recipe.getInputs();
		assertTrue("Firework recipe must fit the crafting grid", inputs.size() <= crafting.getSizeInventory());
		for (int slot = 0; slot < inputs.size(); slot++) {
			List<ItemStack> input = inputs.get(slot);
			int inputVariation = variation % input.size();
			crafting.setInventorySlotContents(slot, input.get(inputVariation).copy());
		}

		RecipeFireworks vanillaRecipe = new RecipeFireworks();
		assertTrue("Displayed firework inputs must match Minecraft's recipe", vanillaRecipe.matches(crafting, null));
		ItemStack actual = vanillaRecipe.getCraftingResult(crafting);
		ItemStack expected = recipe.getOutputs().get(variation);

		assertTrue("Displayed firework output must match Minecraft's recipe", ItemStack.areItemStacksEqual(expected, actual));
		assertEquals("Displayed firework output count", expected.getCount(), actual.getCount());
	}

	private static class TestContainer extends Container {
		@Override
		public boolean canInteractWith(EntityPlayer player) {
			return false;
		}
	}
}
