package mezz.jei.plugins.vanilla;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import com.google.common.collect.Ordering;
import mezz.jei.api.gui.IAdvancedGuiHandler;

class InventoryEffectRendererGuiHandler implements IAdvancedGuiHandler<InventoryEffectRenderer> {
	@Override
	public Class<InventoryEffectRenderer> getGuiContainerClass() {
		return InventoryEffectRenderer.class;
	}

	/**
	 * Modeled after {@link InventoryEffectRenderer#drawActivePotionEffects()}
	 */
	@Override
	public List<Rectangle> getGuiExtraAreas(InventoryEffectRenderer guiContainer) {
		Collection<PotionEffect> activePotionEffects = guiContainer.mc.player.getActivePotionEffects();
		if (activePotionEffects.isEmpty()) {
			return Collections.emptyList();
		}

		List<Rectangle> areas = new ArrayList<>();
		int x = guiContainer.getGuiLeft() - 124;
		int y = guiContainer.getGuiTop();
		int height = 33;
		if (activePotionEffects.size() > 5) {
			height = 132 / (activePotionEffects.size() - 1);
		}
		for (PotionEffect potioneffect : Ordering.natural().sortedCopy(activePotionEffects)) {
			Potion potion = potioneffect.getPotion();
			if (potion.shouldRender(potioneffect)) {
				areas.add(new Rectangle(x, y, 166, 140));
				y += height;
			}
		}
		return areas;
	}
}
