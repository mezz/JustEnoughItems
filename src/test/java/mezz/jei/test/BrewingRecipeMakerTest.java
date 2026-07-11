package mezz.jei.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import mezz.jei.Internal;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeMaker;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeUtil;
import mezz.jei.plugins.vanilla.brewing.BrewingRecipeWrapper;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.startup.StackHelper;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BrewingRecipeMakerTest {
	@Before
	public void setup() {
		if (!Bootstrap.isRegistered()) {
			Bootstrap.register();
		}
		Internal.setStackHelper(new StackHelper(new SubtypeRegistry()));
	}

	@Test
	public void addModdedBrewingRecipesAddsValidRecipe() {
		ItemStack input = waterBottle();
		ItemStack ingredient = new ItemStack(Items.NETHER_WART);
		ItemStack output = potion(PotionTypes.AWKWARD);
		BrewingRecipe brewingRecipe = new BrewingRecipe(input, ingredient, output);
		List<BrewingRecipeWrapper> recipes = new ArrayList<>();

		BrewingRecipeMaker.addModdedBrewingRecipes(Collections.singletonList(brewingRecipe), recipes);

		Assert.assertEquals(1, recipes.size());
		Assert.assertTrue(recipes.contains(new BrewingRecipeWrapper(Collections.singletonList(ingredient), input, output)));
	}

	@Test
	public void addModdedBrewingRecipesSkipsEmptyIngredient() {
		BrewingRecipe brewingRecipe = new BrewingRecipe(waterBottle(), ItemStack.EMPTY, potion(PotionTypes.AWKWARD));
		List<BrewingRecipeWrapper> recipes = new ArrayList<>();

		BrewingRecipeMaker.addModdedBrewingRecipes(Collections.singletonList(brewingRecipe), recipes);

		Assert.assertTrue(recipes.isEmpty());
	}

	@Test
	public void addModdedBrewingRecipesSkipsEmptyInput() {
		BrewingRecipe brewingRecipe = new BrewingRecipe(ItemStack.EMPTY, new ItemStack(Items.NETHER_WART), potion(PotionTypes.AWKWARD));
		List<BrewingRecipeWrapper> recipes = new ArrayList<>();

		BrewingRecipeMaker.addModdedBrewingRecipes(Collections.singletonList(brewingRecipe), recipes);

		Assert.assertTrue(recipes.isEmpty());
	}

	@Test
	public void addModdedBrewingRecipesSkipsEmptyOutput() {
		BrewingRecipe brewingRecipe = new BrewingRecipe(waterBottle(), new ItemStack(Items.NETHER_WART), ItemStack.EMPTY);
		List<BrewingRecipeWrapper> recipes = new ArrayList<>();

		BrewingRecipeMaker.addModdedBrewingRecipes(Collections.singletonList(brewingRecipe), recipes);

		Assert.assertTrue(recipes.isEmpty());
	}

	@Test
	public void addModdedBrewingRecipesSkipsUnsupportedRecipeClass() {
		List<BrewingRecipeWrapper> recipes = new ArrayList<>();

		BrewingRecipeMaker.addModdedBrewingRecipes(Arrays.asList(new UnsupportedBrewingRecipe(), new UnsupportedBrewingRecipe()), recipes);

		Assert.assertTrue(recipes.isEmpty());
	}

	@Test
	public void addModdedBrewingRecipesKeepsExistingRecipesWhenSkippingInvalidOnes() {
		ItemStack existingInput = waterBottle();
		ItemStack existingIngredient = new ItemStack(Items.REDSTONE);
		ItemStack existingOutput = potion(PotionTypes.MUNDANE);
		BrewingRecipeWrapper existingRecipe = new BrewingRecipeWrapper(Collections.singletonList(existingIngredient), existingInput, existingOutput);
		Collection<BrewingRecipeWrapper> recipes = new ArrayList<>(Collections.singletonList(existingRecipe));

		BrewingRecipe invalidRecipe = new BrewingRecipe(waterBottle(), new ItemStack(Items.NETHER_WART), ItemStack.EMPTY);
		ItemStack validIngredient = new ItemStack(Items.GLOWSTONE_DUST);
		ItemStack validOutput = potion(PotionTypes.THICK);
		BrewingRecipe validRecipe = new BrewingRecipe(waterBottle(), validIngredient, validOutput);

		BrewingRecipeMaker.addModdedBrewingRecipes(Arrays.asList(invalidRecipe, validRecipe), recipes);

		Assert.assertEquals(2, recipes.size());
		Assert.assertTrue(recipes.contains(existingRecipe));
		Assert.assertTrue(recipes.contains(new BrewingRecipeWrapper(Collections.singletonList(validIngredient), waterBottle(), validOutput)));
	}

	private static ItemStack waterBottle() {
		return BrewingRecipeUtil.WATER_BOTTLE.copy();
	}

	private static ItemStack potion(net.minecraft.potion.PotionType potionType) {
		return PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), potionType);
	}

	private static class UnsupportedBrewingRecipe implements IBrewingRecipe {
		@Override
		public boolean isInput(@Nonnull ItemStack input) {
			return true;
		}

		@Override
		public boolean isIngredient(@Nonnull ItemStack ingredient) {
			return true;
		}

		@Nonnull
		@Override
		public ItemStack getOutput(@Nonnull ItemStack input, @Nonnull ItemStack ingredient) {
			return potion(PotionTypes.AWKWARD);
		}
	}
}
