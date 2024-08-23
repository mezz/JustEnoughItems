package mezz.jei.forge.platform;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;
import java.util.Optional;

public class RenderHelper implements IPlatformRenderHelper {
	@Override
	public Font getFontRenderer(Minecraft minecraft, ItemStack itemStack) {
		IClientItemExtensions renderProperties = IClientItemExtensions.of(itemStack);
		Font fontRenderer = renderProperties.getFont(itemStack, IClientItemExtensions.FontContext.TOOLTIP);
		if (fontRenderer != null) {
			return fontRenderer;
		}
		return minecraft.font;
	}

	@Override
	public boolean shouldRender(MobEffectInstance potionEffect) {
		IClientMobEffectExtensions effectRenderer = IClientMobEffectExtensions.of(potionEffect);
		return effectRenderer.isVisibleInInventory(potionEffect);
	}

	@Override
	public TextureAtlasSprite getParticleIcon(BakedModel bakedModel) {
		return bakedModel.getParticleIcon(ModelData.EMPTY);
	}

	@Override
	public ItemColors getItemColors() {
		return Minecraft.getInstance().getItemColors();
	}

	@Override
	public Optional<NativeImage> getMainImage(TextureAtlasSprite sprite) {
		SpriteContents contents = sprite.contents();
		NativeImage[] frames = contents.byMipLevel;
		if (frames.length == 0) {
			return Optional.empty();
		}
		NativeImage frame = frames[0];
		return Optional.ofNullable(frame);
	}

	@Override
	public void renderTooltip(GuiGraphics guiGraphics, List<Either<FormattedText, TooltipComponent>> elements, int x, int y, Font font, ItemStack stack) {
		guiGraphics.renderComponentTooltipFromElements(font, elements, x, y, stack);
	}

	@Override
	public Component getName(TagKey<?> tagKey) {
		String tagTranslationKey = getTagTranslationKey(tagKey);
		return Component.translatableWithFallback(tagTranslationKey, "#" + tagKey.location());
	}


	private static String getTagTranslationKey(TagKey<?> tagKey) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("tag.");
		ResourceLocation registryIdentifier = tagKey.registry().location();
		ResourceLocation tagIdentifier = tagKey.location();
		if (!registryIdentifier.getNamespace().equals(ModIds.MINECRAFT_ID)) {
			stringBuilder.append(registryIdentifier.getNamespace()).append(".");
		}

		String registryId = ResourceLocationUtil.sanitizePath(registryIdentifier.getPath());
		String tagId = ResourceLocationUtil.sanitizePath(tagIdentifier.getPath());

		stringBuilder.append(registryId)
			.append(".")
			.append(tagIdentifier.getNamespace())
			.append(".")
			.append(tagId);
		return stringBuilder.toString();
	}
}
