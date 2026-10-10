package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.ICycleButtonWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public final class RecipeCycleButtonWidget<T> extends AbstractRecipeButtonWidget<ICycleButtonWidget<T>> implements ICycleButtonWidget<T> {
	private final List<T> values;
	private final Consumer<T> onValueChanged;
	private int index;

	public RecipeCycleButtonWidget(List<T> values, T initialValue, int width, int height, Consumer<T> onValueChanged) {
		super(width, height);
		this.values = List.copyOf(values);
		if (this.values.isEmpty()) {
			throw new IllegalArgumentException("Cycle buttons require at least one value");
		}
		this.onValueChanged = Objects.requireNonNull(onValueChanged);
		setValue(initialValue);
	}

	@Override
	protected ICycleButtonWidget<T> getThis() {
		return this;
	}

	@Override
	public ICycleButtonWidget<T> setMessage(Function<T, Component> valueText) {
		Objects.requireNonNull(valueText);
		setContents(new RecipeButtonContents.Text(() -> valueText.apply(getValue())));
		return this;
	}

	@Override
	public ICycleButtonWidget<T> setIcon(Function<T, IDrawable> valueIcon) {
		Objects.requireNonNull(valueIcon);
		setContents(new RecipeButtonContents.Icon(() -> valueIcon.apply(getValue())));
		return this;
	}

	@Override
	public T getValue() {
		return values.get(index);
	}

	@Override
	public ICycleButtonWidget<T> setValue(T value) {
		int newIndex = values.indexOf(Objects.requireNonNull(value));
		if (newIndex < 0) {
			throw new IllegalArgumentException("Value must be one of the cycle button's supplied values");
		}
		index = newIndex;
		return this;
	}

	@Override
	protected void onPress(IJeiUserInput input) {
		T previous = getValue();
		int direction = 1;
		if (Screen.hasShiftDown()) {
			direction = -1;
		}
		index = Math.floorMod(index + direction, values.size());
		T selected = getValue();
		if (!Objects.equals(previous, selected)) {
			onValueChanged.accept(selected);
		}
	}
}
