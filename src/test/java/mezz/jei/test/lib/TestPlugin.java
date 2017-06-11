package mezz.jei.test.lib;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class TestPlugin implements IModPlugin {
	public static final int BASE_INGREDIENT_COUNT = 2;

	@Override
	public void registerIngredients(IModIngredientRegistration ingredientRegistry) {
		Collection<TestIngredient> baseTestIngredients = new ArrayList<>();
		for (int i = 0; i < BASE_INGREDIENT_COUNT; i++) {
			baseTestIngredients.add(new TestIngredient(i));
		}

		ingredientRegistry.register(TestIngredient.class, baseTestIngredients, new TestIngredientHelper(), new TestIngredientRenderer());
	}

	private static class TestIngredientRenderer implements IIngredientRenderer<TestIngredient> {
		@Override
		public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable TestIngredient ingredient) {
			// test ingredient is never rendered
		}

		@Override
		public List<String> getTooltip(Minecraft minecraft, TestIngredient ingredient, ITooltipFlag tooltipFlag) {
			return Collections.singletonList("Test Ingredient Tooltip " + ingredient);
		}
	}

	private static class TestIngredientHelper implements IIngredientHelper<TestIngredient> {
		@Override
		public List<TestIngredient> expandSubtypes(List<TestIngredient> ingredients) {
			return ingredients;
		}

		@Nullable
		@Override
		public TestIngredient getMatch(Iterable<TestIngredient> ingredients, TestIngredient ingredientToMatch) {
			for (TestIngredient ingredient : ingredients) {
				if (ingredient.getNumber() == ingredientToMatch.getNumber()) {
					return ingredient;
				}
			}
			return null;
		}

		@Override
		public String getDisplayName(TestIngredient ingredient) {
			return "Test Ingredient Display Name " + ingredient;
		}

		@Override
		public String getUniqueId(TestIngredient ingredient) {
			return "Test Ingredient Unique Id " + ingredient;
		}

		@Override
		public String getWildcardId(TestIngredient ingredient) {
			return "Test Ingredient Unique Id";
		}

		@Override
		public String getModId(TestIngredient ingredient) {
			return "JEI Test Mod";
		}

		@Override
		public Iterable<Color> getColors(TestIngredient ingredient) {
			return Collections.singleton(Color.BLACK);
		}

		@Override
		public String getResourceId(TestIngredient ingredient) {
			return "Test Ingredient Resource Id " + ingredient;
		}

		@Override
		public ItemStack cheatIngredient(TestIngredient ingredient, boolean fullStack) {
			return ItemStack.EMPTY;
		}

		@Override
		public TestIngredient copyIngredient(TestIngredient ingredient) {
			return ingredient.copy();
		}

		@Override
		public String getErrorInfo(TestIngredient ingredient) {
			return "Test Ingredient Error Info " + ingredient;
		}
	}
}
