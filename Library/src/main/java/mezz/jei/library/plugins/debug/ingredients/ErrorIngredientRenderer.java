package mezz.jei.library.plugins.debug.ingredients;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class ErrorIngredientRenderer implements IIngredientRenderer<ErrorIngredient> {
	private final IIngredientHelper<ErrorIngredient> ingredientHelper;

	public ErrorIngredientRenderer(IIngredientHelper<ErrorIngredient> ingredientHelper) {
		this.ingredientHelper = ingredientHelper;
	}

	@Override
	public void render(GuiGraphicsExtractor guiGraphics, ErrorIngredient ingredient) {
		Minecraft minecraft = Minecraft.getInstance();
		if (ingredient.crashType() == ErrorIngredient.CrashType.TooltipCrash) {
			Font font = getFontRenderer(minecraft, ingredient);
			guiGraphics.text(font, "JEI", 0, 0, 0xFFFF0000, false);
			guiGraphics.text(font, "TEST", 0, 8, 0xFFFF0000, false);
		}
	}

	@Override
	public List<Component> getTooltip(ErrorIngredient ingredient, TooltipFlag tooltipFlag) {
		if (ingredient.crashType() == ErrorIngredient.CrashType.TooltipCrash) {
			throw new RuntimeException("intentional tooltip crash for testing");
		}
		List<Component> tooltip = new ArrayList<>();
		String displayName = ingredientHelper.getDisplayName(ingredient);
		tooltip.add(Component.literal(displayName));
		MutableComponent debugIngredient = Component.literal("error ingredient");
		tooltip.add(debugIngredient.withStyle(ChatFormatting.GRAY));
		return tooltip;
	}
}
