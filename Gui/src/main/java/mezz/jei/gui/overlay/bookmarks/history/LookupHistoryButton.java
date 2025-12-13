package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.gui.elements.IIconButtonDescriptor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class LookupHistoryButton implements IIconButtonDescriptor {
	private final IDrawable offIcon;
	private final IDrawable onIcon;
	private final IClientConfig clientConfig;

	public LookupHistoryButton(IClientConfig clientConfig) {
		Textures textures = Internal.getTextures();
		this.offIcon = textures.getHistoryButtonDisabledIcon();
		this.onIcon = textures.getHistoryButtonEnabledIcon();
		this.clientConfig = clientConfig;
	}

	@Override
	public IDrawable getIcon() {
		if (clientConfig.isLookupHistoryEnabled()) {
			return onIcon;
		}
		return offIcon;
	}

	@Override
	public void getTooltips(ITooltipBuilder tooltip) {
		if (clientConfig.isLookupHistoryEnabled()) {
			tooltip.add(Component.translatable("jei.tooltip.lookupHistory.disable"));
		} else {
			tooltip.add(Component.translatable("jei.tooltip.lookupHistory.enable"));
		}
		tooltip.add(
			Component.translatable("jei.tooltip.lookupHistory.usage")
				.withStyle(ChatFormatting.ITALIC)
				.withStyle(ChatFormatting.GRAY)
		);
	}

	@Override
	public boolean isForcePressed() {
		return clientConfig.isLookupHistoryEnabled();
	}

	@Override
	public boolean onPress(IJeiUserInput input) {
		if (!input.isSimulate()) {
			clientConfig.setLookupHistoryEnabled(!clientConfig.isLookupHistoryEnabled());
		}
		return true;
	}
}
