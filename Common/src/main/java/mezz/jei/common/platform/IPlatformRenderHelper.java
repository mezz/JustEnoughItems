package mezz.jei.common.platform;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public interface IPlatformRenderHelper {
	Font getFontRenderer(Minecraft minecraft, ItemStack itemStack);

	boolean shouldRender(MobEffectInstance potionEffect);

	TextureAtlasSprite getParticleIcon(BakedModel bakedModel);

	ItemColors getItemColors();

	Optional<NativeImage> getMainImage(TextureAtlasSprite sprite);

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	void renderTooltip(Screen screen, GuiGraphics guiGraphics, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, int x, int y, Font font, ItemStack stack);

	void renderTooltip(Screen screen, GuiGraphics guiGraphics, List<ClientTooltipComponent> components, int x, int y, Font font, ItemStack stack);

}
