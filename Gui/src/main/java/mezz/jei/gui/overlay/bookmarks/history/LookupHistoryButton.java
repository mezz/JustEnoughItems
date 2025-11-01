package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.input.UserInput;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class LookupHistoryButton extends GuiIconToggleButton {

	public static LookupHistoryButton create(IClientConfig clientConfig) {
		Textures textures = Internal.getTextures();
		IDrawableStatic offIcon = textures.getHistoryButtonDisabledIcon();
		IDrawableStatic onIcon = textures.getHistoryButtonEnabledICon();
		return new LookupHistoryButton(offIcon, onIcon, clientConfig);
	}

	private final IClientConfig clientConfig;

	private LookupHistoryButton(IDrawable offIcon, IDrawable onIcon, IClientConfig clientConfig) {
		super(offIcon, onIcon);
		this.clientConfig = clientConfig;
	}

	@Override
	protected void getTooltips(JeiTooltip tooltip) {
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
	protected boolean isIconToggledOn() {
		return clientConfig.isLookupHistoryEnabled();
	}

	@Override
	protected boolean onMouseClicked(UserInput input) {
		if (!input.isSimulate()) {
			clientConfig.setLookupHistoryEnabled(!clientConfig.isLookupHistoryEnabled());
		}
		return true;
	}
}
