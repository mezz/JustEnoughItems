package mezz.jei.gui.recipes;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.config.IJeiClientConfigs;
import mezz.jei.common.config.RecipeSorterStage;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import net.minecraft.network.chat.Component;

import java.util.Set;

public class RecipeSortStateButtonController implements IIconButtonController {
	private final IDrawable offIcon;
	private final IDrawable onIcon;
	private final RecipeSorterStage recipeSorterStage;
	private final Component disabledTooltip;
	private final Component enabledTooltip;
	private final Runnable onValueChanged;
	private boolean toggledOn;

	public RecipeSortStateButtonController(
		RecipeSorterStage recipeSorterStage,
		IDrawable offIcon,
		IDrawable onIcon,
		Component disabledTooltip,
		Component enabledTooltip,
		Runnable onValueChanged
	) {
		this.offIcon = offIcon;
		this.onIcon = onIcon;
		this.recipeSorterStage = recipeSorterStage;
		this.disabledTooltip = disabledTooltip;
		this.enabledTooltip = enabledTooltip;
		this.onValueChanged = onValueChanged;
	}

	@Override
	public void getTooltips(ITooltipBuilder tooltip) {
		if (toggledOn) {
			tooltip.add(enabledTooltip);
		} else {
			tooltip.add(disabledTooltip);
		}
	}

	@Override
	public void updateState(IButtonState state) {
		IJeiClientConfigs jeiClientConfigs = Internal.getJeiClientConfigs();
		IClientConfig clientConfig = jeiClientConfigs.getClientConfig();
		Set<RecipeSorterStage> recipeSorterStages = clientConfig.getRecipeSorterStages();
		boolean toggledOn = recipeSorterStages.contains(recipeSorterStage);
		if (toggledOn != this.toggledOn) {
			this.toggledOn = toggledOn;
			this.onValueChanged.run();
		}
		if (toggledOn) {
			state.setForcePressed(true);
			state.setIcon(onIcon);
		} else {
			state.setForcePressed(false);
			state.setIcon(offIcon);
		}
	}

	@Override
	public boolean onPress(IJeiUserInput input) {
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
