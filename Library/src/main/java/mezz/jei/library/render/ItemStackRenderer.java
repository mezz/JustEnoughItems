package mezz.jei.library.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.rendering.IIngredientRenderContext;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.render.batch.ItemStackBatchRendererCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemStackRenderer implements IIngredientRenderer<ItemStack> {
	private static final Logger LOGGER = LogManager.getLogger();
	private final ItemStackBatchRendererCache batchRenderer = new ItemStackBatchRendererCache();

	@Override
	@Deprecated(since = "11.81.0", forRemoval = true)
	public void render(PoseStack poseStack, @Nullable ItemStack ingredient) {
		render(poseStack, ingredient, 0, 0, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	@Deprecated(since = "11.81.0", forRemoval = true)
	public void render(PoseStack poseStack, @Nullable ItemStack ingredient, int posX, int posY) {
		render(poseStack, ingredient, posX, posY, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	public void render(PoseStack poseStack, @Nullable ItemStack ingredient, int posX, int posY, IIngredientRenderContext context) {
		if (ingredient != null) {
			PoseStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushPose();
			{
				modelViewStack.mulPoseMatrix(poseStack.last().pose());

				RenderSystem.enableDepthTest();

				Minecraft minecraft = Minecraft.getInstance();
				ItemRenderer itemRenderer = minecraft.getItemRenderer();
				itemRenderer.renderAndDecorateFakeItem(ingredient, posX, posY);
				RenderSystem.disableBlend();
			}
			modelViewStack.popPose();
			// Restore model-view matrix now that the item has been rendered
			RenderSystem.applyModelViewMatrix();
		}
	}

	@Override
	@Deprecated(since = "11.81.0", forRemoval = true)
	public void renderBatch(PoseStack poseStack, List<BatchRenderElement<ItemStack>> elements) {
		renderBatch(poseStack, elements, IIngredientRenderContext.INGREDIENT);
	}

	@Override
	public void renderBatch(PoseStack poseStack, List<BatchRenderElement<ItemStack>> elements, IIngredientRenderContext context) {
		batchRenderer.renderBatch(poseStack, this, elements);
	}

	@Override
	@Deprecated(since = "11.59.0", forRemoval = true)
	@SuppressWarnings("removal")
	public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		return getTooltip(ingredient, minecraft.player, tooltipFlag);
	}

	@Override
	@Deprecated(since = "11.59.0", forRemoval = true)
	@SuppressWarnings("removal")
	public void getTooltip(ITooltipBuilder tooltip, ItemStack ingredient, TooltipFlag tooltipFlag) {
		Minecraft minecraft = Minecraft.getInstance();
		getTooltip(tooltip, ingredient, minecraft.player, tooltipFlag);
	}

	@Override
	public List<Component> getTooltip(ItemStack ingredient, @Nullable Player player, TooltipFlag tooltipFlag) {
		try {
			return ingredient.getTooltipLines(player, tooltipFlag);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(ingredient);
			LOGGER.error("Failed to get tooltip: {}", itemStackInfo, e);
			List<Component> list = new ArrayList<>();
			MutableComponent crash = Component.translatable("jei.tooltip.error.crash");
			list.add(crash.withStyle(ChatFormatting.RED));
			return list;
		}
	}

	public Font getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		return renderHelper.getFontRenderer(minecraft, ingredient);
	}

}
