package mezz.jei.render;

import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.nio.ByteBuffer;

public class ItemStackFastRenderer extends IngredientRenderer<ItemStack> {
	private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	private static final WorldVertexBufferUploader VBO_UPLOADER = new WorldVertexBufferUploader();

	private final IBakedModel bakedModel;
	@Nullable
	private ReusableBufferBuilder bufferBuilder;

	public ItemStackFastRenderer(IIngredientListElement<ItemStack> itemStackElement, IBakedModel bakedModel) {
		super(itemStackElement);
		this.bakedModel = bakedModel;
	}

	public void renderItemAndEffectIntoGUI() {
		try {
			uncheckedRenderItemAndEffectIntoGUI();
		} catch (RuntimeException | LinkageError e) {
			throw createRenderIngredientException(e, element);
		}
	}

	private void uncheckedRenderItemAndEffectIntoGUI() {
		if (Config.isEditModeEnabled()) {
			renderEditMode(element, area, padding);
			GlStateManager.enableBlend();
		}

		ItemStack itemStack = element.getIngredient();
		IBakedModel bakedModel = this.bakedModel;

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(area.x + padding + 8.0f, area.y + padding + 8.0f, 150.0F);
			GlStateManager.scale(16F, -16F, 16F);
			bakedModel = ForgeHooksClient.handleCameraTransforms(bakedModel, ItemCameraTransforms.TransformType.GUI, false);
			GlStateManager.translate(-0.5F, -0.5F, -0.5F);

			Minecraft minecraft = Minecraft.getMinecraft();
			RenderItem renderItem = minecraft.getRenderItem();

			if (this.bufferBuilder == null) {
				bufferBuilder = new ReusableBufferBuilder();
				renderModel(renderItem, bufferBuilder, bakedModel, -1, itemStack);
			}
			VBO_UPLOADER.draw(bufferBuilder);

			if (itemStack.hasEffect()) {
				renderEffect(bakedModel);
			}
		}
		GlStateManager.popMatrix();
	}

	private void renderModel(RenderItem renderItem, BufferBuilder bufferBuilder, IBakedModel model, int color, ItemStack stack) {
		bufferBuilder.begin(7, DefaultVertexFormats.ITEM);

		for (EnumFacing enumfacing : EnumFacing.values()) {
			renderItem.renderQuads(bufferBuilder, model.getQuads(null, enumfacing, 0L), color, stack);
		}

		renderItem.renderQuads(bufferBuilder, model.getQuads(null, null, 0L), color, stack);
		bufferBuilder.finishDrawing();
	}

	protected void renderEffect(IBakedModel model) {
		Minecraft minecraft = Minecraft.getMinecraft();
		TextureManager textureManager = minecraft.getTextureManager();
		RenderItem renderItem = minecraft.getRenderItem();

		GlStateManager.depthMask(false);
		GlStateManager.depthFunc(514);
		GlStateManager.blendFunc(768, 1);
		textureManager.bindTexture(RES_ITEM_GLINT);
		GlStateManager.matrixMode(5890);

		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
		GlStateManager.translate(f, 0.0F, 0.0F);
		GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
		renderItem.renderModel(model, -8372020);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
		GlStateManager.translate(-f1, 0.0F, 0.0F);
		GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
		renderItem.renderModel(model, -8372020);
		GlStateManager.popMatrix();

		GlStateManager.matrixMode(5888);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
	}

	public void renderOverlay() {
		ItemStack itemStack = element.getIngredient();
		try {
			renderOverlay(itemStack, area, padding);
		} catch (RuntimeException | LinkageError e) {
			throw createRenderIngredientException(e, element);
		}
	}

	private void renderOverlay(ItemStack itemStack, Rectangle area, int padding) {
		FontRenderer font = getFontRenderer(itemStack);
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.renderItemOverlayIntoGUI(font, itemStack, area.x + padding, area.y + padding, null);
	}

	public static FontRenderer getFontRenderer(ItemStack itemStack) {
		Item item = itemStack.getItem();
		FontRenderer fontRenderer = item.getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = Minecraft.getMinecraft().fontRenderer;
		}
		return fontRenderer;
	}

	private static class ReusableBufferBuilder extends BufferBuilder {

		public ReusableBufferBuilder() {
			super(16384);
		}

		@Override
		public void reset() {
			// do not reset, this gets reused
		}

		/**
		 * Same as {@link BufferBuilder#growBuffer(int)} but grows it 2x each time instead of by 2097152.
		 */
		@Override
		protected void growBuffer(int p_181670_1_) {
			if (MathHelper.roundUp(p_181670_1_, 4) / 4 > this.rawIntBuffer.remaining() || this.getVertexCount() * this.getVertexFormat().getNextOffset() + p_181670_1_ > this.byteBuffer.capacity()) {
				int i = this.byteBuffer.capacity();
				int j = i * 2;
				int k = this.rawIntBuffer.position();
				ByteBuffer bytebuffer = GLAllocation.createDirectByteBuffer(j);
				this.byteBuffer.position(0);
				bytebuffer.put(this.byteBuffer);
				bytebuffer.rewind();
				this.byteBuffer = bytebuffer;
				this.rawFloatBuffer = this.byteBuffer.asFloatBuffer().asReadOnlyBuffer();
				this.rawIntBuffer = this.byteBuffer.asIntBuffer();
				this.rawIntBuffer.position(k);
				this.rawShortBuffer = this.byteBuffer.asShortBuffer();
				this.rawShortBuffer.position(k << 1);
			}
		}
	}
}
