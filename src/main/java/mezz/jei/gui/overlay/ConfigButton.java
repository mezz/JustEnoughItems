package mezz.jei.gui.overlay;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.Internal;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.config.Config;
import mezz.jei.config.JEIModConfigGui;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.input.MouseHelper;
import mezz.jei.util.Translator;

public class ConfigButton extends GuiIconToggleButton {
	public static ConfigButton create(IngredientListOverlay parent) {
		GuiHelper guiHelper = Internal.getHelpers().getGuiHelper();
		return new ConfigButton(guiHelper.getConfigButtonIcon(), guiHelper.getConfigButtonCheatIcon(), parent);
	}

	private final IngredientListOverlay parent;

	private ConfigButton(IDrawable disabledIcon, IDrawable enabledIcon, IngredientListOverlay parent) {
		super(disabledIcon, enabledIcon);
		this.parent = parent;
	}

	@Override
	protected void getTooltips(List<String> tooltip) {
		tooltip.add(Translator.translateToLocal("jei.tooltip.config"));
		if (!Config.isOverlayEnabled()) {
			tooltip.add(TextFormatting.GOLD + Translator.translateToLocal("jei.tooltip.ingredient.list.disabled"));
			tooltip.add(TextFormatting.GOLD + Translator.translateToLocalFormatted("jei.tooltip.ingredient.list.disabled.how.to.fix", KeyBindings.toggleOverlay.getDisplayName()));
		} else if (!parent.hasRoom()) {
			tooltip.add(TextFormatting.GOLD + Translator.translateToLocal("jei.tooltip.not.enough.space"));
		}
		if (Config.isCheatItemsEnabled()) {
			tooltip.add(TextFormatting.RED + Translator.translateToLocal("jei.tooltip.cheat.mode.button.enabled"));
			KeyBinding toggleCheatMode = KeyBindings.toggleCheatMode;
			if (toggleCheatMode.getKeyCode() != 0) {
				tooltip.add(TextFormatting.RED + Translator.translateToLocalFormatted("jei.tooltip.cheat.mode.how.to.disable.hotkey", toggleCheatMode.getDisplayName()));
			} else if (KeyBindings.toggleCheatModeConfigButton.getKeyCode() != 0) {
				tooltip.add(TextFormatting.RED + Translator.translateToLocalFormatted("jei.tooltip.cheat.mode.how.to.disable.hotkey", KeyBindings.toggleCheatModeConfigButton.getDisplayName()));
			}
		}
	}

	public boolean handleKeyPressed(int eventKey) {
		if (Config.isOverlayEnabled() && isMouseOver(MouseHelper.getX(), MouseHelper.getY()) &&
			KeyBindings.toggleCheatModeConfigButton.isActiveAndMatches(eventKey)) {
			Config.toggleCheatItemsEnabled();
			return true;
		}
		return false;
	}

	@Override
	protected boolean isIconToggledOn() {
		return Config.isCheatItemsEnabled();
	}

	@Override
	protected boolean onMouseClicked(int mouseX, int mouseY) {
		if (Config.isOverlayEnabled()) {
			Minecraft minecraft = Minecraft.getMinecraft();
			if (minecraft.currentScreen != null) {
				GuiScreen configScreen = new JEIModConfigGui(minecraft.currentScreen);
				parent.updateScreen(configScreen, false);
				minecraft.displayGuiScreen(configScreen);
			}
			return true;
		}
		return false;
	}
}
