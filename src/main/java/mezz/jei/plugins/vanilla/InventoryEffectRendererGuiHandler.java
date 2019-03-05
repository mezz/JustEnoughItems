package mezz.jei.plugins.vanilla;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import com.google.common.collect.Ordering;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;

class InventoryEffectRendererGuiHandler implements IGuiContainerHandler<InventoryEffectRenderer> {
	/**
	 * Modeled after {@link InventoryEffectRenderer#drawActivePotionEffects()}
	 */
	@Override
	public List<Rectangle2d> getGuiExtraAreas(InventoryEffectRenderer guiContainer) {
		Collection<PotionEffect> activePotionEffects = guiContainer.mc.player.getActivePotionEffects();
		if (activePotionEffects.isEmpty()) {
			return Collections.emptyList();
		}

		List<Rectangle2d> areas = new ArrayList<>();
		int x = guiContainer.getGuiLeft() - 124;
		int y = guiContainer.getGuiTop();
		int height = 33;
		if (activePotionEffects.size() > 5) {
			height = 132 / (activePotionEffects.size() - 1);
		}
		for (PotionEffect potioneffect : Ordering.natural().sortedCopy(activePotionEffects)) {
			Potion potion = potioneffect.getPotion();
			if (potion.shouldRender(potioneffect)) {
				areas.add(new Rectangle2d(x, y, 166, 140));
				y += height;
			}
		}
		return areas;
	}
}
