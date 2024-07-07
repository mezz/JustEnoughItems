package mezz.jei.gui.recipes;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.input.UserInput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Set;

public class RecipeSortStateButton extends GuiIconToggleButton {
	private final RecipeSorterStage recipeSorterStage;
	private final Component disabledTooltip;
	private final Component enabledTooltip;
	private final Runnable onValueChanged;
	private boolean toggledOn;

	public RecipeSortStateButton(
		RecipeSorterStage recipeSorterStage,
		IDrawable offIcon,
		IDrawable onIcon,
		Component disabledTooltip,
		Component enabledTooltip,
		Runnable onValueChanged
	) {
		super(offIcon, onIcon);
		this.recipeSorterStage = recipeSorterStage;
		this.disabledTooltip = disabledTooltip;
		this.enabledTooltip = enabledTooltip;
		this.onValueChanged = onValueChanged;

		tick();
	}

	@Override
	protected void getTooltips(List<Component> tooltip) {
		if (toggledOn) {
			tooltip.add(enabledTooltip);
		} else {
			tooltip.add(disabledTooltip);
		}
	}

	@Override
	public void tick() {
		IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		Set<RecipeSorterStage> recipeSorterStages = clientConfig.getRecipeSorterStages();
		boolean toggledOn = recipeSorterStages.contains(recipeSorterStage);
		if (toggledOn != this.toggledOn) {
			this.toggledOn = toggledOn;
			this.onValueChanged.run();
		}
	}

	@Override
	protected boolean isIconToggledOn() {
		return toggledOn;
	}

	@Override
	protected boolean onMouseClicked(UserInput input) {
		if (!input.isSimulate()) {
			IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
			IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
			if (this.toggledOn) {
				clientConfig.disableRecipeSorterStage(recipeSorterStage);
				this.toggledOn = false;
			} else {
				clientConfig.enableRecipeSorterStage(recipeSorterStage);
				this.toggledOn = true;
			}
			this.onValueChanged.run();
		}
		return true;
	}
}
