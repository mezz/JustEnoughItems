package mezz.jei.render;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ISlowRenderItem;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.input.ClickedIngredient;
import mezz.jei.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class IngredientListBatchRenderer {
	private static final Logger LOGGER = LogManager.getLogger();

	private final List<IngredientListSlot> slots = new ArrayList<>();

	private final List<ItemStackFastRenderer> renderItems2d = new ArrayList<>();
	private final List<ItemStackFastRenderer> renderItems3d = new ArrayList<>();
	private final List<IngredientListElementRenderer<?>> renderOther = new ArrayList<>();
	private final IEditModeConfig editModeConfig;
	private final IWorldConfig worldConfig;

	private int blocked = 0;

	public IngredientListBatchRenderer(IEditModeConfig editModeConfig, IWorldConfig worldConfig) {
		this.editModeConfig = editModeConfig;
		this.worldConfig = worldConfig;
	}

	public void clear() {
		slots.clear();

		renderItems2d.clear();
		renderItems3d.clear();
		renderOther.clear();
		blocked = 0;
	}

	public int size() {
		return slots.size() - blocked;
	}

	public void add(IngredientListSlot ingredientListSlot) {
		slots.add(ingredientListSlot);
	}

	public List<IngredientListSlot> getAllGuiIngredientSlots() {
		return slots;
	}

	public void set(final int startIndex, List<IIngredientListElement<?>> ingredientList) {
		renderItems2d.clear();
		renderItems3d.clear();
		renderOther.clear();
		blocked = 0;

		int i = startIndex;
		for (IngredientListSlot ingredientListSlot : slots) {
			if (ingredientListSlot.isBlocked()) {
				ingredientListSlot.clear();
				blocked++;
			} else {
				if (i >= ingredientList.size()) {
					ingredientListSlot.clear();
				} else {
					IIngredientListElement<?> element = ingredientList.get(i);
					set(ingredientListSlot, element);
				}
				i++;
			}
		}
	}

	private <V> void set(IngredientListSlot ingredientListSlot, IIngredientListElement<V> element) {
		ingredientListSlot.clear();

		V ingredient = element.getIngredient();
		if (ingredient instanceof ItemStack) {
			//noinspection unchecked
			IIngredientListElement<ItemStack> itemStackElement = (IIngredientListElement<ItemStack>) element;
			ItemStack itemStack = itemStackElement.getIngredient();
			Minecraft minecraft = Minecraft.getInstance();
			ItemRenderer itemRenderer = minecraft.getItemRenderer();
			BakedModel bakedModel;
			try {
				bakedModel = itemRenderer.getModel(itemStack, null, null, 0);
				Preconditions.checkNotNull(bakedModel, "IBakedModel must not be null.");
			} catch (Throwable throwable) {
				String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
				LOGGER.error("ItemStack crashed getting IBakedModel. {}", stackInfo, throwable);
				return;
			}

			if (!bakedModel.isCustomRenderer() && !bakedModel.isLayered() && !(itemStack.getItem() instanceof ISlowRenderItem)) {
				ItemStackFastRenderer renderer = new ItemStackFastRenderer(itemStackElement);
				ingredientListSlot.setIngredientRenderer(renderer);
				if (bakedModel.usesBlockLight()) {
					renderItems3d.add(renderer);
				} else {
					renderItems2d.add(renderer);
				}
				return;
			}
		}

		IngredientListElementRenderer<V> renderer = new IngredientListElementRenderer<>(element);
		ingredientListSlot.setIngredientRenderer(renderer);
		renderOther.add(renderer);
	}

	@Nullable
	public ClickedIngredient<?> getIngredientUnderMouse(double mouseX, double mouseY) {
		IngredientListElementRenderer<?> hovered = getHovered(mouseX, mouseY);
		if (hovered != null) {
			IIngredientListElement<?> element = hovered.getElement();
			return ClickedIngredient.create(element.getIngredient(), hovered.getArea());
		}
		return null;
	}

	@Nullable
	public IngredientListElementRenderer<?> getHovered(double mouseX, double mouseY) {
		for (IngredientListSlot slot : slots) {
			if (slot.isMouseOver(mouseX, mouseY)) {
				return slot.getIngredientRenderer();
			}
		}
		return null;
	}

	public <T> Optional<IngredientListElementRenderer<T>> getHovered(double mouseX, double mouseY, IIngredientType<T> ingredientType) {
		return this.slots.stream()
			.filter(s -> s.isMouseOver(mouseX, mouseY))
			.map(s -> s.getIngredientRenderer(ingredientType))
			.filter(Objects::nonNull)
			.findFirst();
	}

	/**
	 * renders all ItemStacks
	 */
	public void render(Minecraft minecraft, PoseStack poseStack) {

		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		itemRenderer.blitOffset += 50.0F;

		// Most of this code can be found in ItemRenderer#renderGuiItem, and is intended to
		// speed up state-setting by grouping similar items.
		TextureManager textureManager = minecraft.getTextureManager();
		textureManager.getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		//TODO - 1.17: Figure out if we are properly factoring the base pose stack into account when calling render
		// in ItemStackFastRenderer, as there is a decent chance that we are not fully taking it into account for how
		// the MVM is done.

		MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();

		for (ItemStackFastRenderer slot : renderItems3d) {
			slot.renderItemAndEffectIntoGUI(buffer, poseStack, editModeConfig, worldConfig);
		}
		renderBatch(itemRenderer, buffer);

		// 2d Items
		Lighting.setupForFlatItems();
		for (ItemStackFastRenderer slot : renderItems2d) {
			slot.renderItemAndEffectIntoGUI(buffer, poseStack, editModeConfig, worldConfig);
		}
		renderBatch(itemRenderer, buffer);

		// Default is 3d lighting, see ItemRenderer
		Lighting.setupFor3DItems();

		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();

		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		textureManager.getTexture(InventoryMenu.BLOCK_ATLAS).restoreLastBlurMipmap();

		itemRenderer.blitOffset -= 50.0F;

		// overlays
		for (ItemStackFastRenderer slot : renderItems3d) {
			slot.renderOverlay();
		}

		for (ItemStackFastRenderer slot : renderItems2d) {
			slot.renderOverlay();
		}

		// Restore model-view matrix now that all items have been rendered
		RenderSystem.applyModelViewMatrix();

		// other rendering
		for (IngredientListElementRenderer<?> slot : renderOther) {
			slot.renderSlow(poseStack, editModeConfig, worldConfig);
		}
	}

	private static void renderBatch(ItemRenderer itemRenderer, MultiBufferSource.BufferSource buffer) {
		//Apply changes to MVM AFTER the rendering so that the edit mode overlay draws properly
		// but before we draw the batch of items as the batch is drawn against the MVM
		PoseStack modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushPose();
		modelViewStack.translate(16, 0, 100 + itemRenderer.blitOffset);
		modelViewStack.scale(16, -16, 16);
		modelViewStack.translate(-0.5, -0.5, -0.5);
		RenderSystem.applyModelViewMatrix();
		buffer.endBatch();
		modelViewStack.popPose();
		RenderSystem.applyModelViewMatrix();
	}
}
