package mezz.jei.gui.overlay;

import java.util.List;

import mezz.jei.config.JEIClientConfig;
import mezz.jei.input.click.MouseClickState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.textures.Textures;
import net.minecraft.util.text.TranslationTextComponent;

public class ConfigButton extends GuiIconToggleButton {
	public static ConfigButton create(IngredientListOverlay parent, IWorldConfig worldConfig) {
		Textures textures = Internal.getTextures();
		return new ConfigButton(textures.getConfigButtonIcon(), textures.getConfigButtonCheatIcon(), parent, worldConfig);
	}

	private final IngredientListOverlay parent;
	private final IWorldConfig worldConfig;

	private ConfigButton(IDrawable disabledIcon, IDrawable enabledIcon, IngredientListOverlay parent, IWorldConfig worldConfig) {
		super(disabledIcon, enabledIcon);
		this.parent = parent;
		this.worldConfig = worldConfig;
	}

	@Override
	protected void getTooltips(List<ITextComponent> tooltip) {
		tooltip.add(new TranslationTextComponent("jei.tooltip.config"));
		if (!worldConfig.isOverlayEnabled()) {
			TranslationTextComponent disabled = new TranslationTextComponent("jei.tooltip.ingredient.list.disabled");
			TranslationTextComponent overLay = new TranslationTextComponent(KeyBindings.toggleOverlay.saveString());
			TranslationTextComponent disabledFix = new TranslationTextComponent("jei.tooltip.ingredient.list.disabled.how.to.fix", overLay);
			tooltip.add(disabled.withStyle(TextFormatting.GOLD));
			tooltip.add(disabledFix.withStyle(TextFormatting.GOLD));
		} else if (!parent.isListDisplayed()) {
			TranslationTextComponent notEnoughSpace = new TranslationTextComponent("jei.tooltip.not.enough.space");
			tooltip.add(notEnoughSpace.withStyle(TextFormatting.GOLD));
		}
		if (worldConfig.isCheatItemsEnabled()) {
			TranslationTextComponent enabled = new TranslationTextComponent("jei.tooltip.cheat.mode.button.enabled");
			tooltip.add(enabled.withStyle(TextFormatting.RED));
			KeyBinding toggleCheatMode = KeyBindings.toggleCheatMode;
			if (!toggleCheatMode.isUnbound()) {
				TranslationTextComponent cheatMode = new TranslationTextComponent(toggleCheatMode.saveString());
				TranslationTextComponent disableHotkey = new TranslationTextComponent("jei.tooltip.cheat.mode.how.to.disable.hotkey", cheatMode);
				tooltip.add(disableHotkey.withStyle(TextFormatting.RED));
			} else {
				TranslationTextComponent controlKeyLocalization = new TranslationTextComponent(Minecraft.ON_OSX ? "key.jei.ctrl.mac" : "key.jei.ctrl");
				TranslationTextComponent noHotKey = new TranslationTextComponent("jei.tooltip.cheat.mode.how.to.disable.no.hotkey", controlKeyLocalization);
				tooltip.add(noHotKey.withStyle(TextFormatting.RED));
			}
		}
	}

	@Override
	protected boolean isIconToggledOn() {
		return worldConfig.isCheatItemsEnabled();
	}

	@Override
	protected boolean onMouseClicked(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
		if (worldConfig.isOverlayEnabled()) {
			if (!clickState.isSimulate()) {
				if (Screen.hasControlDown()) {
					worldConfig.toggleCheatItemsEnabled();
				} else {
					JEIClientConfig.openSettings();
				}
			}
			return true;
		}
		return false;
	}
}
