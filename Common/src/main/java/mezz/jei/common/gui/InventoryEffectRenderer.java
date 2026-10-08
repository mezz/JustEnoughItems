package mezz.jei.common.gui;

import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;

import java.util.ArrayList;
import java.util.List;

/** Measures the status effect bars rendered beside an inventory screen. */
public final class InventoryEffectRenderer {
	public static final int COMPACT_WIDTH = 32;
	private static final int WIDE_WIDTH = 120;

	private InventoryEffectRenderer() {}

	/** Returns visible bar bounds in rendering order, using Minecraft's fixed wide or compact dimensions. */
	public static List<ImmutableRect2i> getEffectAreas(AbstractContainerScreen<?> screen, boolean compact) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!(screen instanceof EffectRenderingInventoryScreen<?> effectsScreen) || !effectsScreen.canSeeEffects() || minecraft.player == null) {
			return List.of();
		}
		var screenHelper = Services.PLATFORM.getScreenHelper();
		int x = screenHelper.getGuiLeft(screen) + screenHelper.getXSize(screen) + 2;
		int availableWidth = screen.width - x;
		var effects = minecraft.player.getActiveEffects().stream().sorted().toList();
		int yStep = 33;
		if (effects.size() > 5) {
			yStep = 132 / (effects.size() - 1);
		}
		int width = COMPACT_WIDTH;
		if (!compact && availableWidth >= WIDE_WIDTH) {
			width = WIDE_WIDTH;
		}
		int y = screenHelper.getGuiTop(screen);
		var renderHelper = Services.PLATFORM.getRenderHelper();
		List<ImmutableRect2i> areas = new ArrayList<>(effects.size());
		for (var effect : effects) {
			if (renderHelper.shouldRender(effect)) {
				areas.add(new ImmutableRect2i(x, y, width, COMPACT_WIDTH));
				y += yStep;
			}
		}
		return areas;
	}
}
