package mezz.jei.plugins.vanilla;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Container;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;

import com.google.common.collect.Ordering;

class InventoryEffectRendererGuiHandler<T extends Container> implements IGuiContainerHandler<DisplayEffectsScreen<T>> {
	/**
	 * Modeled after {@link DisplayEffectsScreen#drawActivePotionEffects()}
	 */
	@Override
	public List<Rectangle2d> getGuiExtraAreas(DisplayEffectsScreen<T> containerScreen) {
		Minecraft minecraft = containerScreen.getMinecraft();
		if (minecraft == null) {
			return Collections.emptyList();
		}
		ClientPlayerEntity player = minecraft.player;
		if (player == null) {
			return Collections.emptyList();
		}
		Collection<EffectInstance> activePotionEffects = player.getActivePotionEffects();
		if (activePotionEffects.isEmpty()) {
			return Collections.emptyList();
		}

		List<Rectangle2d> areas = new ArrayList<>();
		int x = containerScreen.getGuiLeft() - 124;
		int y = containerScreen.getGuiTop();
		int height = 33;
		if (activePotionEffects.size() > 5) {
			height = 132 / (activePotionEffects.size() - 1);
		}
		for (EffectInstance potioneffect : Ordering.natural().sortedCopy(activePotionEffects)) {
			Effect potion = potioneffect.getPotion();
			if (potion.shouldRender(potioneffect)) {
				areas.add(new Rectangle2d(x, y, 166, 140));
				y += height;
			}
		}
		return areas;
	}
}
