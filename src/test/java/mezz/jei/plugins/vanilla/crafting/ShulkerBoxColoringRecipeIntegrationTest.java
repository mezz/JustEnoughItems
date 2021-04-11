package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.item.crafting.ShulkerBoxRecipes;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShulkerBoxColoringRecipeIntegrationTest {
	@BeforeClass
	public static void bootstrapMinecraft() {
		if (!Bootstrap.isRegistered()) {
			Bootstrap.register();
		}
	}

	@Test
	public void everyDisplayedBoxAndDyeCraftsTheDisplayedColor() {
		List<ItemStack> dyes = new ArrayList<>();
		for (EnumDyeColor color : EnumDyeColor.values()) {
			dyes.add(new ItemStack(Items.DYE, 1, color.getDyeDamage()));
		}
		List<ShapelessRecipes> recipes = ShulkerBoxColoringRecipeMaker.getShulkerBoxColoringRecipes(dyes);
		ShulkerBoxRecipes.ShulkerBoxColoring vanillaRecipe = new ShulkerBoxRecipes.ShulkerBoxColoring();

		assertEquals("Every dye color needs a displayed recipe", 16, recipes.size());
		for (ShapelessRecipes recipe : recipes) {
			ItemStack[] boxes = recipe.getIngredients().get(0).getMatchingStacks();
			ItemStack[] matchingDyes = recipe.getIngredients().get(1).getMatchingStacks();
			assertEquals("All existing box colors can be recolored", 16, boxes.length);
			for (ItemStack box : boxes) {
				for (ItemStack dye : matchingDyes) {
					InventoryCrafting crafting = new InventoryCrafting(new TestContainer(), 3, 3);
					crafting.setInventorySlotContents(0, box.copy());
					crafting.setInventorySlotContents(1, dye.copy());

					assertTrue("Displayed ingredients must match Minecraft's recipe", vanillaRecipe.matches(crafting, null));
					ItemStack actual = vanillaRecipe.getCraftingResult(crafting);

					assertTrue("Displayed color must match Minecraft's result", ItemStack.areItemStacksEqual(recipe.getRecipeOutput(), actual));
				}
			}
		}
	}

	private static class TestContainer extends Container {
		@Override
		public boolean canInteractWith(EntityPlayer player) {
			return false;
		}
	}
}
