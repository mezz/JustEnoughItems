package mezz.jei.plugins.vanilla;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.effect.MobEffectInstance;

import net.minecraftforge.client.EffectRenderer;
import net.minecraftforge.client.RenderProperties;

class InventoryEffectRendererGuiHandler<T extends AbstractContainerMenu> implements IGuiContainerHandler<EffectRenderingInventoryScreen<T>> {
	/**
	 * Modeled after {@link DisplayEffectsScreen#drawActivePotionEffects()}
	 */
	@SuppressWarnings("JavadocReference")
	@Override
	public List<Rect2i> getGuiExtraAreas(EffectRenderingInventoryScreen<T> containerScreen) {
		Minecraft minecraft = containerScreen.getMinecraft();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return Collections.emptyList();
		}
		Collection<MobEffectInstance> activePotionEffects = player.getActiveEffects();
		if (activePotionEffects.isEmpty()) {
			return Collections.emptyList();
		}

		List<Rect2i> areas = new ArrayList<>();
		int x = containerScreen.getGuiLeft() + containerScreen.getXSize() + 2;
		int y = containerScreen.getGuiTop();
		// JEI always forces the potion effect renderer to "compact" width mode when JEI is open.
		int width = 32;

		int height = 33;
		if (activePotionEffects.size() > 5) {
			height = 132 / (activePotionEffects.size() - 1);
		}
		for (MobEffectInstance potionEffect : activePotionEffects) {
			EffectRenderer effectRenderer = RenderProperties.getEffectRenderer(potionEffect);
			if (effectRenderer.shouldRender(potionEffect)) {
				areas.add(new Rect2i(x, y, width, height));
				y += height;
			}
		}
		return areas;
	}
}
