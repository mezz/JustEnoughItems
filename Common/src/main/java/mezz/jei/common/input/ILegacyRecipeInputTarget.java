package mezz.jei.common.input;

import mezz.jei.common.input.interaction.IInputInteraction;
import net.minecraft.client.gui.screens.Screen;

import java.util.Optional;

/** Retains category input callbacks after recipe controls and JEI shortcuts have declined the input. */
public interface ILegacyRecipeInputTarget extends IInputTarget {
	Optional<IInputInteraction> beginLegacyInput(Screen screen, UserInput input, IInternalKeyMappings keys);
}
