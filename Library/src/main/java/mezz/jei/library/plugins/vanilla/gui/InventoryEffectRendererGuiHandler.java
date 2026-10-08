package mezz.jei.library.plugins.vanilla.gui;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.InventoryEffectRenderer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public final class InventoryEffectRendererGuiHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {
	@Override
	public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> containerScreen) {
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
