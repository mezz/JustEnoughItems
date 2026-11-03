package mezz.jei.library.render;

import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.rendering.IIngredientDecorator;
import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class ItemStackDecorator implements IIngredientDecorator<ItemStack> {
	private final IIngredientRenderer<ItemStack> ingredientRenderer;

	public ItemStackDecorator(IIngredientRenderer<ItemStack> ingredientRenderer) {
		this.ingredientRenderer = ingredientRenderer;
	}

	@Override
	public void render(GuiGraphics guiGraphics, ItemStack ingredient, int x, int y, IIngredientRenderContext context) {
		var font = ingredientRenderer.getFontRenderer(Minecraft.getInstance(), ingredient);
		guiGraphics.renderItemDecorations(font, ingredient, x, y);
	}
}
