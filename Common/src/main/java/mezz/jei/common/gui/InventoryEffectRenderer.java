package mezz.jei.common.gui;

import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

import java.util.ArrayList;
import java.util.List;

/** Measures the status effect bars rendered beside a container screen. */
public final class InventoryEffectRenderer {
	public static final int COMPACT_WIDTH = 32;
	private static final int MIN_WIDE_SPACE = 120;
	private static final int SPACING = 7;

	private InventoryEffectRenderer() {
	}

	/**
	 * Returns the visible bar bounds in rendering order, or an empty list when effects cannot be shown.
	 * When {@code compact} is false, widths follow Minecraft's text measurement and available screen space.
	 */
	public static List<ImmutableRect2i> getEffectAreas(AbstractContainerScreen<?> screen, boolean compact) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!screen.showsActiveEffects() || minecraft.player == null || minecraft.level == null) {
			return List.of();
		}
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		int x = screenHelper.getLeftPos(screen) + screenHelper.getImageWidth(screen) + 2;
		int availableWidth = screen.width - x;
		if (availableWidth < COMPACT_WIDTH) {
			return List.of();
		}
		var renderHelper = Services.PLATFORM.getRenderHelper();
		List<MobEffectInstance> effects = minecraft.player.getActiveEffects().stream()
			.filter(renderHelper::shouldRender)
			.sorted()
			.toList();
		int yStep = 33;
		if (effects.size() > 5) {
			yStep = 132 / (effects.size() - 1);
		}
		int maxWidth = COMPACT_WIDTH;
		if (!compact && availableWidth >= MIN_WIDE_SPACE) {
			maxWidth = availableWidth - SPACING;
		}
		int y = screenHelper.getTopPos(screen);
		Font font = screen.getFont();
		float tickRate = minecraft.level.tickRateManager().tickrate();
		List<ImmutableRect2i> areas = new ArrayList<>(effects.size());
		for (MobEffectInstance effect : effects) {
			Component name = getEffectName(effect);
			Component duration = MobEffectUtil.formatDuration(effect, 1.0F, tickRate);
			int textWidth = Math.max(font.width(name), font.width(duration));
			int width = Math.min(maxWidth, COMPACT_WIDTH + textWidth + SPACING);
			areas.add(new ImmutableRect2i(x, y, width, COMPACT_WIDTH));
			y += yStep;
		}
		return areas;
	}

	private static Component getEffectName(MobEffectInstance effect) {
		MutableComponent name = effect.getEffect().value().getDisplayName().copy();
		if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
			name.append(CommonComponents.SPACE)
				.append(Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)));
		}
		return name;
	}
}
