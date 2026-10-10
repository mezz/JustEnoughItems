package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.inputs.IJeiUserInput;

import java.util.Objects;

public final class RecipeButtonWidget extends AbstractRecipeButtonWidget<RecipeButtonWidget> {
	private final Runnable onPress;

	public RecipeButtonWidget(int width, int height, Runnable onPress) {
		super(width, height);
		this.onPress = Objects.requireNonNull(onPress);
	}

	@Override
	protected RecipeButtonWidget getThis() {
		return this;
	}

	@Override
	protected void onPress(IJeiUserInput input) {
		onPress.run();
	}
}
