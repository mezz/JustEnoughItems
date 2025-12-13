package mezz.jei.gui.elements;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;

import java.util.function.Function;

public class BasicButtonDescriptor implements IIconButtonDescriptor {
	private final IDrawable icon;
	private final Function<IJeiUserInput, Boolean> onPress;

	public BasicButtonDescriptor(IDrawable icon, Function<IJeiUserInput, Boolean> onPress) {
		this.icon = icon;
		this.onPress = onPress;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public boolean onPress(IJeiUserInput input) {
		return onPress.apply(input);
	}
}
