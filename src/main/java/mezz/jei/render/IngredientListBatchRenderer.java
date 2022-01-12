package mezz.jei.render;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ISlowRenderItem;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.ingredients.IngredientInfo;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.ingredients.IngredientTypeHelper;
import mezz.jei.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class IngredientListBatchRenderer {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int BLACKLIST_COLOR = 0xFFFF0000;

	private final List<IngredientListSlot> slots = new ArrayList<>();

	private final List<IngredientListElementRenderer<ItemStack>> renderItems2d = new ArrayList<>();
	private final List<IngredientListElementRenderer<ItemStack>> renderItems3d = new ArrayList<>();
	private final IngredientListBatches renderOther = new IngredientListBatches();
	private final IClientConfig clientConfig;
	private final IEditModeConfig editModeConfig;
	private final IWorldConfig worldConfig;
	private final IngredientManager ingredientManager;

	private int blocked = 0;

	public IngredientListBatchRenderer(IClientConfig clientConfig, IEditModeConfig editModeConfig, IWorldConfig worldConfig, IngredientManager ingredientManager) {
		this.clientConfig = clientConfig;
		this.editModeConfig = editModeConfig;
		this.worldConfig = worldConfig;
		this.ingredientManager = ingredientManager;
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

	public void set(final int startIndex, List<?> ingredientList) {
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
					Object ingredient = ingredientList.get(i);
					set(ingredientListSlot, ingredient);
				}
				i++;
			}
		}
	}

	private <V> void set(IngredientListSlot ingredientListSlot, V ingredient) {
		ingredientListSlot.clear();

		if (ingredient instanceof ItemStack itemStack) {
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

			if (
				!bakedModel.isCustomRenderer() &&
				!bakedModel.isLayered() &&
				!(itemStack.getItem() instanceof ISlowRenderItem)
			) {
				IngredientListElementRenderer<ItemStack> renderer = new IngredientListElementRenderer<>(itemStack);
				ingredientListSlot.setIngredientRenderer(renderer);
				if (bakedModel.usesBlockLight()) {
					renderItems3d.add(renderer);
				} else {
					renderItems2d.add(renderer);
				}
				return;
			}
		}

		IngredientListElementRenderer<V> renderer = new IngredientListElementRenderer<>(ingredient);
		ingredientListSlot.setIngredientRenderer(renderer);
		IIngredientType<V> ingredientType = ingredientManager.getIngredientType(ingredient);
		renderOther.put(ingredientType, renderer);
	}

	public Optional<IngredientListElementRenderer<?>> getHovered(double mouseX, double mouseY) {
		return getHoveredStream(mouseX, mouseY)
			.findFirst();
	}

	public <T> Optional<IngredientListElementRenderer<T>> getHovered(double mouseX, double mouseY, IIngredientType<T> ingredientType) {
		return getHoveredStream(mouseX, mouseY)
			.map(ingredientRenderer -> IngredientTypeHelper.checkedCast(ingredientRenderer, ingredientType))
			.flatMap(Optional::stream)
			.findFirst();
	}

	private Stream<IngredientListElementRenderer<?>> getHoveredStream(double mouseX, double mouseY) {
		return slots.stream()
			.filter(s -> s.isMouseOver(mouseX, mouseY))
			.map(IngredientListSlot::getIngredientRenderer)
			.flatMap(Optional::stream);
	}

	/**
	 * renders all ItemStacks
	 */
	public void render(Minecraft minecraft, PoseStack poseStack) {
		if (clientConfig.isFastItemRenderingEnabled()) {
			// optimized batch rendering
			renderBatchedItemStacks(minecraft, poseStack);
		} else {
			IngredientInfo<ItemStack> ingredientInfo = ingredientManager.getIngredientInfo(VanillaTypes.ITEM);
			IIngredientRenderer<ItemStack> ingredientRenderer = ingredientInfo.getIngredientRenderer();
			IIngredientHelper<ItemStack> ingredientHelper = ingredientInfo.getIngredientHelper();
			for (IngredientListElementRenderer<ItemStack> slot : renderItems3d) {
				renderIngredient(poseStack, slot, ingredientRenderer, ingredientHelper);
			}
			for (IngredientListElementRenderer<ItemStack> slot : renderItems2d) {
				renderIngredient(poseStack, slot, ingredientRenderer, ingredientHelper);
			}
		}

		// normal rendering
		for (IIngredientType<?> ingredientType : renderOther.getTypes()) {
			renderIngredientType(poseStack, ingredientType);
		}
	}

	public void renderBatchedItemStacks(Minecraft minecraft, PoseStack poseStack) {
		if (renderItems2d.isEmpty() && renderItems3d.isEmpty()) {
			return;
		}

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

		MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
		IngredientInfo<ItemStack> registeredItemstack = ingredientManager.getIngredientInfo(VanillaTypes.ITEM);
		IIngredientHelper<ItemStack> itemStackHelper = registeredItemstack.getIngredientHelper();
		IIngredientRenderer<ItemStack> itemStackRenderer = registeredItemstack.getIngredientRenderer();

		renderItemStackBatch(poseStack, itemRenderer, buffer, itemStackHelper, renderItems3d);

		Lighting.setupForFlatItems();
		renderItemStackBatch(poseStack, itemRenderer, buffer, itemStackHelper, renderItems2d);

		// Default is 3d lighting, see ItemRenderer
		Lighting.setupFor3DItems();

		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();

		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		textureManager.getTexture(InventoryMenu.BLOCK_ATLAS).restoreLastBlurMipmap();

		itemRenderer.blitOffset -= 50.0F;

		// overlays
		for (IngredientListElementRenderer<ItemStack> slot : renderItems3d) {
			renderOverlay(itemStackRenderer, slot);
		}

		for (IngredientListElementRenderer<ItemStack> slot : renderItems2d) {
			renderOverlay(itemStackRenderer, slot);
		}

		// Restore model-view matrix now that all items have been rendered
		RenderSystem.applyModelViewMatrix();
	}

	private void renderItemStackFast(
		PoseStack poseStack,
		ItemRenderer itemRenderer,
		MultiBufferSource.BufferSource buffer,
		IIngredientHelper<ItemStack> itemStackHelper,
		IngredientListElementRenderer<ItemStack> slot
	) {
		ItemStack itemStack = slot.getIngredient();
		if (worldConfig.isEditModeEnabled()) {
			renderEditMode(poseStack, slot.getArea(), slot.getPadding(), editModeConfig, itemStack, itemStackHelper);
			RenderSystem.enableBlend();
		}

		Rect2i area = slot.getArea();
		int padding = slot.getPadding();
		try {
			BakedModel bakedModel = itemRenderer.getModel(itemStack, null, null, 0);
			poseStack.pushPose();
			poseStack.translate((area.getX() + padding) / 16D, (area.getY() + padding) / -16D, 0);
			itemRenderer.render(itemStack, ItemTransforms.TransformType.GUI, false, poseStack, buffer, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
			poseStack.popPose();
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, itemStack);
		}
	}

	private static void renderOverlay(IIngredientRenderer<ItemStack> renderer, IngredientListElementRenderer<ItemStack> slot) {
		ItemStack itemStack = slot.getIngredient();
		Rect2i area = slot.getArea();
		int padding = slot.getPadding();
		try {
			Minecraft minecraft = Minecraft.getInstance();
			Font font = renderer.getFontRenderer(minecraft, itemStack);
			ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
			itemRenderer.renderGuiItemDecorations(font, itemStack, area.getX() + padding, area.getY() + padding, null);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, itemStack);
		}
	}

	private <T> void renderIngredientType(PoseStack poseStack, IIngredientType<T> ingredientType) {
		List<IngredientListElementRenderer<T>> slots = renderOther.get(ingredientType);
		IngredientInfo<T> ingredientInfo = ingredientManager.getIngredientInfo(ingredientType);
		IIngredientRenderer<T> ingredientRenderer = ingredientInfo.getIngredientRenderer();
		IIngredientHelper<T> ingredientHelper = ingredientInfo.getIngredientHelper();
		for (IngredientListElementRenderer<T> slot : slots) {
			renderIngredient(poseStack, slot, ingredientRenderer, ingredientHelper);
		}
	}

	private <T> void renderIngredient(PoseStack poseStack, IngredientListElementRenderer<T> slot, IIngredientRenderer<T> ingredientRenderer, IIngredientHelper<T> ingredientHelper) {
		T ingredient = slot.getIngredient();
		Rect2i area = slot.getArea();

		if (worldConfig.isEditModeEnabled()) {
			renderEditMode(poseStack, area, slot.getPadding(), editModeConfig, ingredient, ingredientHelper);
			RenderSystem.enableBlend();
		}
		try {
			ingredientRenderer.render(poseStack, area.getX() + slot.getPadding(), area.getY() + slot.getPadding(), ingredient);
		} catch (RuntimeException | LinkageError e) {
			throw ErrorUtil.createRenderIngredientException(e, ingredient);
		}
	}

	private static <T> void renderEditMode(PoseStack poseStack, Rect2i area, int padding, IEditModeConfig editModeConfig, T ingredient, IIngredientHelper<T> ingredientHelper) {
		if (editModeConfig.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
			GuiComponent.fill(poseStack, area.getX() + padding, area.getY() + padding, area.getX() + 16 + padding, area.getY() + 16 + padding, BLACKLIST_COLOR);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
	}

	private void renderItemStackBatch(
		PoseStack poseStack,
		ItemRenderer itemRenderer,
		MultiBufferSource.BufferSource buffer,
		IIngredientHelper<ItemStack> itemStackHelper,
		List<IngredientListElementRenderer<ItemStack>> slots
	) {
		for (IngredientListElementRenderer<ItemStack> slot : slots) {
			renderItemStackFast(poseStack, itemRenderer, buffer, itemStackHelper, slot);
		}

		//Apply changes to MVM AFTER the rendering so that the edit mode overlay draws properly,
		//but before we draw the batch of items as the batch is drawn against the MVM
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
