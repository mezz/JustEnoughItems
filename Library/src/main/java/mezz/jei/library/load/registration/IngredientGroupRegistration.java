package mezz.jei.library.load.registration;

import com.google.common.collect.ImmutableListMultimap;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IIngredientGroupRegistration;
import mezz.jei.common.ingredients.group.DynamicSelector;
import mezz.jei.common.ingredients.group.IIngredientGroupSelector;
import mezz.jei.common.ingredients.group.IngredientsSelector;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.collect.ListMultiMap;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.SequencedCollection;
import java.util.function.Predicate;

public class IngredientGroupRegistration implements IIngredientGroupRegistration {

	private final ListMultiMap<Identifier, IIngredientGroupSelector> ingredientGroups = new ListMultiMap<>();

	@Override
	public <T> void addGroup(Identifier id, SequencedCollection<? extends ITypedIngredient<? extends T>> ingredients) {
		ErrorUtil.checkNotNull(id, "id");
		ErrorUtil.checkNotNull(ingredients, "ingredients");
		ErrorUtil.checkNotEmpty(ingredients, "ingredients");
		ingredientGroups.put(id, new IngredientsSelector(List.copyOf(ingredients)));
	}

	@Override
	public <T> void addGroupSelector(Identifier id, IIngredientType<T> ingredientType, Predicate<T> selector) {
		ErrorUtil.checkNotNull(id, "id");
		ErrorUtil.checkNotNull(ingredientType, "ingredientType");
		ErrorUtil.checkNotNull(selector, "selector");
		ingredientGroups.put(id, new DynamicSelector<>(ingredientType, selector));
	}

	@Unmodifiable
	public ImmutableListMultimap<Identifier, IIngredientGroupSelector> getIngredientGroups() {
		return ingredientGroups.toImmutable();
	}

}
