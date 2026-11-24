package mezz.jei.library.render;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.rendering.IIngredientDecorator;
import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class ItemStackDecorator implements IIngredientDecorator<ItemStack> {
	private static final int MINECRAFT_ITEM_COUNT_Z = 200;

	private final IIngredientRenderer<ItemStack> ingredientRenderer;

	public ItemStackDecorator(IIngredientRenderer<ItemStack> ingredientRenderer) {
		this.ingredientRenderer = ingredientRenderer;
	}

	@Override
	public void render(GuiGraphics guiGraphics, ItemStack ingredient, int x, int y, IIngredientRenderContext context) {
		var font = ingredientRenderer.getFontRenderer(Minecraft.getInstance(), ingredient);
		var pose = guiGraphics.pose();
		pose.pushPose();
		try {
			// Compensate for Minecraft's count offset so stack counts stay at JEI's chosen decoration depth.
			pose.translate(0, 0, -MINECRAFT_ITEM_COUNT_Z);
			guiGraphics.renderItemDecorations(font, ingredient, x, y);
		} finally {
			pose.popPose();
		}
	}
}
