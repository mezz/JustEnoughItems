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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
		// temporary hack while we wait for https://github.com/MinecraftForge/MinecraftForge/pull/10055

		Screen screen = Minecraft.getInstance().screen;
		if (screen == null) {
			return;
		}
		guiGraphics.tooltipStack = stack;
		List<ClientTooltipComponent> components = gatherTooltipComponents(stack, elements, x, screen.width, screen.height, font);
		guiGraphics.renderTooltipInternal(font, components, x, y, DefaultTooltipPositioner.INSTANCE);
		guiGraphics.tooltipStack = ItemStack.EMPTY;
	}

	@SuppressWarnings("All")
	private static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack, List<Either<FormattedText, TooltipComponent>> elements, int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
		Font font = ForgeHooksClient.getTooltipFont(stack, fallbackFont);
		RenderTooltipEvent.GatherComponents event = new RenderTooltipEvent.GatherComponents(stack, screenWidth, screenHeight, elements, -1);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) {
			return List.of();
		} else {
			int tooltipTextWidth = event.getTooltipElements().stream().mapToInt((either) -> {
				Objects.requireNonNull(font);
				return (Integer)either.map(font::width, (component) -> {
					return 0;
				});
			}).max().orElse(0);
			boolean needsWrap = false;
			int tooltipX = mouseX + 12;
			if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
				tooltipX = mouseX - 16 - tooltipTextWidth;
				if (tooltipX < 4) {
					if (mouseX > screenWidth / 2) {
						tooltipTextWidth = mouseX - 12 - 8;
					} else {
						tooltipTextWidth = screenWidth - 16 - mouseX;
					}

					needsWrap = true;
				}
			}

			if (event.getMaxWidth() > 0 && tooltipTextWidth > event.getMaxWidth()) {
				tooltipTextWidth = event.getMaxWidth();
				needsWrap = true;
			}

			int tooltipTextWidthF = tooltipTextWidth;
			return needsWrap ? event.getTooltipElements().stream().flatMap((either) -> {
				return (Stream)either.map((text) -> {
					return splitLine(text, font, tooltipTextWidthF);
				}, (component) -> {
					return Stream.of(ClientTooltipComponent.create(component));
				});
			}).toList() : event.getTooltipElements().stream().map((either) -> {
				return (ClientTooltipComponent)either.map((text) -> {
					return ClientTooltipComponent.create(text instanceof Component ? ((Component)text).getVisualOrderText() : Language.getInstance().getVisualOrder(text));
				}, ClientTooltipComponent::create);
			}).toList();
		}
	}

	private static Stream<ClientTooltipComponent> splitLine(FormattedText text, Font font, int maxWidth) {
		if (text instanceof Component component) {
			if (component.getString().isEmpty()) {
				return Stream.of(component.getVisualOrderText()).map(ClientTooltipComponent::create);
			}
		}

		return font.split(text, maxWidth).stream().map(ClientTooltipComponent::create);
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
