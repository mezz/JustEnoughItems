package mezz.jei.test.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

@JeiPlugin
public class TestPlugin implements IModPlugin {
	public static final int BASE_INGREDIENT_COUNT = 2;

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "test");
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		Collection<TestIngredient> baseTestIngredients = new ArrayList<>();
		for (int i = 0; i < BASE_INGREDIENT_COUNT; i++) {
			baseTestIngredients.add(new TestIngredient(i));
		}

		registration.register(TestIngredient.TYPE, baseTestIngredients, new TestIngredientHelper(), new TestIngredientRenderer());
	}

	private static class TestIngredientRenderer implements IIngredientRenderer<TestIngredient> {
		@Override
		public void render(PoseStack matrixStack, int xPosition, int yPosition, @Nullable TestIngredient ingredient) {
			// test ingredient is never rendered
		}

		@Override
		public List<Component> getTooltip(TestIngredient ingredient, TooltipFlag tooltipFlag) {
			return Collections.singletonList(new TextComponent("Test Ingredient Tooltip " + ingredient));
		}
	}

}
