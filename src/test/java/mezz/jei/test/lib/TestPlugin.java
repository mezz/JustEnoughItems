package mezz.jei.test.lib;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;

@JEIPlugin
public class TestPlugin implements IModPlugin {
	public static final int BASE_INGREDIENT_COUNT = 2;

	@Override
	public void registerIngredients(IModIngredientRegistration ingredientRegistry) {
		Collection<TestIngredient> baseTestIngredients = new ArrayList<>();
		for (int i = 0; i < BASE_INGREDIENT_COUNT; i++) {
			baseTestIngredients.add(new TestIngredient(i));
		}

		ingredientRegistry.register(TestIngredient.TYPE, baseTestIngredients, new TestIngredientHelper(), new TestIngredientRenderer());
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

}
