package mezz.jei.library.plugins.jei.ingredients;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class DebugIngredientRenderer implements IIngredientRenderer<DebugIngredient> {
	private final IIngredientHelper<DebugIngredient> ingredientHelper;

	public DebugIngredientRenderer(IIngredientHelper<DebugIngredient> ingredientHelper) {
		this.ingredientHelper = ingredientHelper;
	}

	@Override
	public void render(GuiGraphics guiGraphics, DebugIngredient ingredient) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = getFontRenderer(minecraft, ingredient);
		guiGraphics.drawString(font, "JEI", 0, 0, 0xFFFF0000, false);
		guiGraphics.drawString(font, "#" + ingredient.getNumber(), 0, 8, 0xFFFF0000, false);
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	@Override
	public List<Component> getTooltip(DebugIngredient ingredient, TooltipFlag tooltipFlag) {
		List<Component> tooltip = new ArrayList<>();
		String displayName = ingredientHelper.getDisplayName(ingredient);
		tooltip.add(Component.literal(displayName));
		MutableComponent debugIngredient = Component.literal("debug ingredient");
		tooltip.add(debugIngredient.withStyle(ChatFormatting.GRAY));
		return tooltip;
	}
}
