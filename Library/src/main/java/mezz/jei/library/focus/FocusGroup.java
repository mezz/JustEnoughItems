package mezz.jei.library.focus;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class FocusGroup implements IFocusGroup {
	public static final IFocusGroup EMPTY = new FocusGroup(List.of());

	/**
	 * Make sure any IFocus coming in through API calls is validated
	 */
	public static IFocusGroup create(Collection<? extends IFocus<?>> focuses, IIngredientManager ingredientManager) {
		List<Focus<?>> checkedFocuses = focuses.stream()
			.filter(Objects::nonNull)
			.<Focus<?>>map(f -> Focus.checkOne(f, ingredientManager))
			.toList();
		if (checkedFocuses.isEmpty()) {
			return EMPTY;
		}
		return new FocusGroup(checkedFocuses);
	}

	/**
	 * Make sure any IFocusGroup coming in through API calls is validated.
	 */
	public static IFocusGroup checkOne(IFocusGroup focusGroup, IIngredientManager ingredientManager) {
		ErrorUtil.checkNotNull(focusGroup, "focusGroup");
		if (focusGroup instanceof FocusGroup) {
			return focusGroup;
		}
		return create(focusGroup.getAllFocuses(), ingredientManager);
	}

	private final List<IFocus<?>> focuses;

	private FocusGroup(List<Focus<?>> focuses) {
		this.focuses = List.copyOf(focuses);
	}

	@Override
	public boolean isEmpty() {
		return focuses.isEmpty();
	}

	@Override
	public List<IFocus<?>> getAllFocuses() {
		return focuses;
	}

	@Override
	public Stream<IFocus<?>> getFocuses(RecipeIngredientRole role) {
		return focuses.stream()
			.filter(focus -> focus.getRole() == role);
	}

	@Override
	public <T> Stream<IFocus<T>> getFocuses(IIngredientType<T> ingredientType) {
		return focuses.stream()
			.map(focus -> focus.checkedCast(ingredientType))
			.flatMap(Optional::stream);
	}

	@Override
	public <T> Stream<IFocus<T>> getFocuses(IIngredientType<T> ingredientType, RecipeIngredientRole role) {
		return getFocuses(role)
			.map(focus -> focus.checkedCast(ingredientType))
			.flatMap(Optional::stream);
	}
}
