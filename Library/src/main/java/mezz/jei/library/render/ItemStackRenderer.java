package mezz.jei.library.render;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.library.render.batch.ItemStackBatchRendererCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	private final ItemStackBatchRendererCache batchRenderer = new ItemStackBatchRendererCache();

	@Override
	@Deprecated(since = "15.75.0", forRemoval = true)
	public void render(GuiGraphics guiGraphics, @Nullable ItemStack ingredient) {
		render(guiGraphics, ingredient, 0, 0, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	@Deprecated(since = "15.75.0", forRemoval = true)
	public void render(GuiGraphics guiGraphics, @Nullable ItemStack ingredient, int posX, int posY) {
		render(guiGraphics, ingredient, posX, posY, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	public void render(GuiGraphics guiGraphics, @Nullable ItemStack ingredient, int posX, int posY, IIngredientRenderContext context) {
		if (ingredient != null) {
			RenderSystem.enableDepthTest();
			guiGraphics.renderFakeItem(ingredient, posX, posY);
			RenderSystem.disableBlend();
		}
	}

	@Override
	@Deprecated(since = "15.75.0", forRemoval = true)
	public void renderBatch(GuiGraphics guiGraphics, List<BatchRenderElement<ItemStack>> elements) {
		renderBatch(guiGraphics, elements, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	public void renderBatch(GuiGraphics guiGraphics, List<BatchRenderElement<ItemStack>> elements, IIngredientRenderContext context) {
		batchRenderer.renderBatch(guiGraphics, elements);
	}

	@Override
	@Deprecated(since = "15.54.0", forRemoval = true)
	@SuppressWarnings("removal")
	public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		return getTooltip(ingredient, minecraft.player, tooltipFlag);
	}

	@Override
	@Deprecated(since = "15.54.0", forRemoval = true)
	@SuppressWarnings("removal")
	public void getTooltip(ITooltipBuilder tooltip, ItemStack ingredient, TooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		getTooltip(tooltip, ingredient, minecraft.player, tooltipFlag);
	}

	@Override
	public List<Component> getTooltip(ItemStack ingredient, @Nullable Player player, TooltipFlag tooltipFlag) {
		return ingredient.getTooltipLines(player, tooltipFlag);
	}

	public Font getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		return renderHelper.getFontRenderer(minecraft, ingredient);
	}

}
