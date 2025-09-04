package mezz.jei.gui.overlay.bookmarks.history;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.input.UserInput;
import net.minecraft.network.chat.Component;

public class HistoryButton extends GuiIconToggleButton {

	public static HistoryButton create(IClientConfig clientConfig) {
		Textures textures = Internal.getTextures();
		IDrawableStatic offIcon = textures.getHistoryButtonDisabledIcon();
		IDrawableStatic onIcon = textures.getHistoryButtonEnabledICon();
		return new HistoryButton(offIcon, onIcon, clientConfig);
	}


	private final IClientConfig clientConfig;

	private HistoryButton(IDrawable offIcon, IDrawable onIcon, IClientConfig clientConfig) {
		super(offIcon, onIcon);
		this.clientConfig = clientConfig;
	}

	@Override
	protected void getTooltips(JeiTooltip tooltip) {
		tooltip.add(Component.translatable("jei.tooltip.history"));
	}

	@Override
	protected boolean isIconToggledOn() {
		return clientConfig.isHistoryEnabled();
	}

	@Override
	protected boolean onMouseClicked(UserInput input) {
		if (!input.isSimulate()) {
			clientConfig.setHistoryEnabled(!clientConfig.isHistoryEnabled());
		}
		return true;
	}
}
