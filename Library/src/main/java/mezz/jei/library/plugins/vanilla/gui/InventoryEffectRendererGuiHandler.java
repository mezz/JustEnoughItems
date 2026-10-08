package mezz.jei.library.plugins.vanilla.gui;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.InventoryEffectRenderer;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

public final class InventoryEffectRendererGuiHandler<T extends AbstractContainerMenu> implements IGuiContainerHandler<EffectRenderingInventoryScreen<T>> {
	@Override
	public List<Rect2i> getGuiExtraAreas(EffectRenderingInventoryScreen<T> containerScreen) {
		if (!Internal.getJeiFeatures().getInventoryEffectRendererGuiHandlerEnabled()) {
			return List.of();
		}
		// Lay out JEI against compact bars when it may compact them. Measuring the expanded bars
		// against that layout avoids changing the exclusions back and forth every frame.
		boolean compact = Internal.getClientConfigs().getClientConfig().compactInventoryEffects().get();
		return InventoryEffectRenderer.getEffectAreas(containerScreen, compact).stream()
			.map(area -> area.toMutable())
			.toList();
	}
}
