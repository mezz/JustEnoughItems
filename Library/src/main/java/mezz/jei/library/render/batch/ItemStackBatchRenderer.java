package mezz.jei.library.render.batch;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import mezz.jei.api.ingredients.rendering.BatchRenderElement;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.library.render.ItemStackRenderer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ItemStackBatchRenderer {
	private final List<ElementWithModel> useBlockLight;
	private final List<ElementWithModel> noBlockLight;
	private final List<BatchRenderElement<ItemStack>> customRender;

	public ItemStackBatchRenderer(Minecraft minecraft, List<BatchRenderElement<ItemStack>> elements) {
		this.useBlockLight = new ArrayList<>();
		this.noBlockLight = new ArrayList<>();
		this.customRender = new ArrayList<>();

		ClientLevel level = minecraft.level;
		ItemRenderer itemRenderer = minecraft.getItemRenderer();

		for (BatchRenderElement<ItemStack> element : elements) {
			ItemStack itemStack = element.ingredient();
			if (!itemStack.isEmpty()) {
				BakedModel bakedmodel = itemRenderer.getModel(itemStack, level, null, 0);
				if (bakedmodel.isCustomRenderer()) {
					customRender.add(element);
				} else if (bakedmodel.usesBlockLight()) {
					ElementWithModel elementWithModel = new ElementWithModel(bakedmodel, itemStack, element.x(), element.y());
					useBlockLight.add(elementWithModel);
				} else {
					if (!bakedmodel.isGui3d()) {
						IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
						bakedmodel = renderHelper.createLimitedQuadItemModel(bakedmodel);
					}
					ElementWithModel elementWithModel = new ElementWithModel(bakedmodel, itemStack, element.x(), element.y());
					noBlockLight.add(elementWithModel);
				}
			}
		}
	}

	public void render(PoseStack poseStack, Minecraft minecraft, ItemRenderer itemRenderer, ItemStackRenderer itemStackRenderer) {
		MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
		if (!noBlockLight.isEmpty()) {
			Lighting.setupForFlatItems();
			for (ElementWithModel element : noBlockLight) {
				renderItem(poseStack, bufferSource, itemRenderer, element.model(), element.stack(), element.x(), element.y());
			}
			bufferSource.endBatch();
			Lighting.setupFor3DItems();
		}

		if (!useBlockLight.isEmpty()) {
			for (ElementWithModel element : useBlockLight) {
				renderItem(poseStack, bufferSource, itemRenderer, element.model(), element.stack(), element.x(), element.y());
			}
			bufferSource.endBatch();
		}

		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		for (ElementWithModel element : useBlockLight) {
			ItemStack ingredient = element.stack();
			Font font = renderHelper.getFontRenderer(minecraft, ingredient);
			itemRenderer.renderGuiItemDecorations(font, ingredient, element.x(), element.y());
		}
		for (ElementWithModel element : noBlockLight) {
			ItemStack ingredient = element.stack();
			Font font = renderHelper.getFontRenderer(minecraft, ingredient);
			itemRenderer.renderGuiItemDecorations(font, ingredient, element.x(), element.y());
		}
		RenderSystem.disableBlend();
		for (BatchRenderElement<ItemStack> element : customRender) {
			ItemStack ingredient = element.ingredient();
			itemStackRenderer.render(poseStack, ingredient, element.x(), element.y());
			RenderSystem.disableBlend();
		}
		RenderSystem.disableBlend();
	}

	private void renderItem(
		PoseStack poseStack,
		MultiBufferSource bufferSource,
		ItemRenderer itemRenderer,
		BakedModel bakedmodel,
		ItemStack itemStack,
		int x,
		int y
	) {
		poseStack.pushPose();
		poseStack.translate(x + 8f, y + 8f, 150f);
		poseStack.mulPoseMatrix(Matrix4f.createScaleMatrix(1.0F, -1.0F, 1.0F));
		poseStack.scale(16.0F, 16.0F, 16.0F);

		try {
			itemRenderer.render(
				itemStack,
				ItemTransforms.TransformType.GUI,
				false,
				poseStack,
				bufferSource,
				0xf000f0,
				OverlayTexture.NO_OVERLAY,
				bakedmodel
			);
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
			CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
			crashreportcategory.setDetail("Item Type", () -> String.valueOf(itemStack.getItem()));
			crashreportcategory.setDetail("Item NBT", () -> String.valueOf(itemStack.getTag()));
			crashreportcategory.setDetail("Item Foil", () -> String.valueOf(itemStack.hasFoil()));
			throw new ReportedException(crashreport);
		}

		poseStack.popPose();
	}
}
