package mezz.jei.plugins.vanilla;

import com.google.common.collect.Ordering;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
		int x = guiContainer.getGuiLeft() - 124;
		int y = guiContainer.getGuiTop();
		Collection<PotionEffect> activePotionEffects = guiContainer.mc.player.getActivePotionEffects();
		if (activePotionEffects.isEmpty()) {
			return Collections.emptyList();
		}
		List<Rectangle> areas = new ArrayList<>();
		int height = 33;
		if (activePotionEffects.size() > 5) {
			height = 132 / (activePotionEffects.size() - 1);
		}

		for (PotionEffect potioneffect : Ordering.natural().sortedCopy(activePotionEffects)) {
			Potion potion = potioneffect.getPotion();
			if (potion.shouldRender(potioneffect)) {
				areas.add(new Rectangle(x, y, 140, 32));
				y += height;
			}
		}
		return areas;
	}
}
