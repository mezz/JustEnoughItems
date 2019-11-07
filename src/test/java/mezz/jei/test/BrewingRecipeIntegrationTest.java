package mezz.jei.test;

import java.util.Arrays;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.plugins.vanilla.VanillaPlugin;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeMaker;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeWrapper;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackHelper;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.startup.ModIngredientRegistration;
import mezz.jei.startup.StackHelper;
import mezz.jei.test.lib.TestModIdHelper;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
import org.junit.Assert;
import org.junit.Test;

public class BrewingRecipeIntegrationTest {
	@Test
	public void includesContainerConversionsForPotionsNotBrewableFromWater() {
		if (!Bootstrap.isRegistered()) {
			Bootstrap.register();
		}
		SubtypeRegistry subtypes = new SubtypeRegistry();
		new VanillaPlugin().registerItemSubtypes(subtypes);
		StackHelper stackHelper = new StackHelper(subtypes);
		Internal.setStackHelper(stackHelper);
		ModIngredientRegistration registration = new ModIngredientRegistration();
		registration.register(VanillaTypes.ITEM, Arrays.asList(new ItemStack(Items.GUNPOWDER), new ItemStack(Items.DRAGON_BREATH)), new ItemStackHelper(stackHelper), new ItemStackRenderer());
		IngredientRegistry ingredients = registration.createIngredientRegistry(new TestModIdHelper(), new IngredientBlacklistInternal());

		List<BrewingRecipeWrapper> recipes = BrewingRecipeMaker.getBrewingRecipes(ingredients);
		BrewingRecipeWrapper luckRecipe = recipes.stream()
			.filter(recipe -> recipe.getPotionInput().getItem() == Items.POTIONITEM)
			.filter(recipe -> PotionUtils.getPotionFromItem(recipe.getPotionInput()) == PotionType.getPotionTypeForName("luck"))
			.filter(recipe -> recipe.getPotionOutput().getItem() == Items.SPLASH_POTION)
			.findFirst()
			.orElseThrow(() -> new AssertionError("Luck potions can become splash potions even though luck cannot be brewed from water"));
		VanillaBrewingRecipe vanilla = new VanillaBrewingRecipe();
		ItemStack actual = vanilla.getOutput(luckRecipe.getPotionInput(), luckRecipe.getIngredients().get(0));
		Assert.assertTrue(ItemStack.areItemStacksEqual(luckRecipe.getPotionOutput(), actual));
	}
}
