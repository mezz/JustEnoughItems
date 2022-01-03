package mezz.jei.gui.overlay;

import mezz.jei.Internal;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.JEIClientConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.textures.Textures;
import mezz.jei.input.UserInput;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

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
			MutableComponent enabled = new TranslatableComponent("jei.tooltip.cheat.mode.button.enabled")
				.withStyle(ChatFormatting.RED);
			tooltip.add(enabled);

			if (!KeyBindings.toggleCheatMode.isUnbound()) {
				MutableComponent component = new TranslatableComponent(
					"jei.tooltip.cheat.mode.how.to.disable.hotkey",
					KeyBindings.toggleCheatMode.getTranslatedKeyMessage()
				).withStyle(ChatFormatting.RED);
				tooltip.add(component);
			} else if (!KeyBindings.toggleCheatModeConfigButton.isUnbound()) {
				MutableComponent component = new TranslatableComponent(
					"jei.tooltip.cheat.mode.how.to.disable.hover.config.button.hotkey",
					KeyBindings.toggleCheatModeConfigButton.getTranslatedKeyMessage()
				).withStyle(ChatFormatting.RED);
				tooltip.add(component);
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
