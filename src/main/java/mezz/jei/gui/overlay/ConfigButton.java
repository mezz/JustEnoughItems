package mezz.jei.gui.overlay;

import java.util.List;

import mezz.jei.config.JEIClientConfig;
import mezz.jei.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.textures.Textures;
import net.minecraft.network.chat.TranslatableComponent;

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
	protected void getTooltips(List<Component> tooltip) {
		tooltip.add(new TranslatableComponent("jei.tooltip.config"));
		if (!worldConfig.isOverlayEnabled()) {
			TranslatableComponent disabled = new TranslatableComponent("jei.tooltip.ingredient.list.disabled");
			TranslatableComponent overLay = new TranslatableComponent(KeyBindings.toggleOverlay.saveString());
			TranslatableComponent disabledFix = new TranslatableComponent("jei.tooltip.ingredient.list.disabled.how.to.fix", overLay);
			tooltip.add(disabled.withStyle(ChatFormatting.GOLD));
			tooltip.add(disabledFix.withStyle(ChatFormatting.GOLD));
		} else if (!parent.isListDisplayed()) {
			TranslatableComponent notEnoughSpace = new TranslatableComponent("jei.tooltip.not.enough.space");
			tooltip.add(notEnoughSpace.withStyle(ChatFormatting.GOLD));
		}
		if (worldConfig.isCheatItemsEnabled()) {
			TranslatableComponent enabled = new TranslatableComponent("jei.tooltip.cheat.mode.button.enabled");
			tooltip.add(enabled.withStyle(ChatFormatting.RED));
			KeyMapping toggleCheatMode = KeyBindings.toggleCheatMode;
			if (!toggleCheatMode.isUnbound()) {
				TranslatableComponent cheatMode = new TranslatableComponent(toggleCheatMode.saveString());
				TranslatableComponent disableHotkey = new TranslatableComponent("jei.tooltip.cheat.mode.how.to.disable.hotkey", cheatMode);
				tooltip.add(disableHotkey.withStyle(ChatFormatting.RED));
			} else {
				TranslatableComponent controlKeyLocalization = new TranslatableComponent(Minecraft.ON_OSX ? "key.jei.ctrl.mac" : "key.jei.ctrl");
				TranslatableComponent noHotKey = new TranslatableComponent("jei.tooltip.cheat.mode.how.to.disable.no.hotkey", controlKeyLocalization);
				tooltip.add(noHotKey.withStyle(ChatFormatting.RED));
			}
		}
	}

	@Override
	protected boolean isIconToggledOn() {
		return worldConfig.isCheatItemsEnabled();
	}

	@Override
	protected boolean onMouseClicked(UserInput input) {
		if (worldConfig.isOverlayEnabled()) {
			if (!input.isSimulate()) {
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
