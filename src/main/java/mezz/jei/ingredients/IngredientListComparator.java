package mezz.jei.ingredients;

import mezz.jei.api.ingredients.ISortableIngredient;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.Log;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class IngredientListComparator implements Comparator<ISortableIngredient> {
	private Map<ResourceLocation, SortEntry<?>> allSorters = new LinkedHashMap<>();
	private List<SortEntry<?>> chosenSorters = new ArrayList<>();

	public void testSort(NonNullList<IIngredientListElement> ingredients) {
		for (SortEntry entry : chosenSorters) {
			try {
				ingredients.sort((Comparator<ISortableIngredient>) (o1, o2) -> {
					IIngredientType ingredientType = entry.getIngredientType();
					if (ingredientType == null || (ingredientType == o1.getIngredientType() && ingredientType == o2.getIngredientType())) {
						@SuppressWarnings("unchecked")
						int comparison = entry.getComparator().compare(o1, o2);
						if (comparison != 0) {
							return comparison;
						}
					}
					return 0;
				});
			} catch (RuntimeException e) {
				Log.get().error("Sorting error: {}", entry.getName(), e);
			}
		}
	}

	@Override
	public int compare(ISortableIngredient o1, ISortableIngredient o2) {
		for (SortEntry entry : chosenSorters) {
			IIngredientType ingredientType = entry.getIngredientType();
			if (ingredientType == null || (ingredientType == o1.getIngredientType() && ingredientType == o2.getIngredientType())) {
				@SuppressWarnings("unchecked")
				int comparison = entry.getComparator().compare(o1, o2);
				if (comparison != 0) {
					return comparison;
				}
			}
		}
		return o1.getCreativeMenuOrder();
	}

	public <T> void addTypedComparison(ResourceLocation name, IIngredientType<T> ingredientType, Comparator<T> comparator) {
		if (allSorters.containsKey(name)) {
			throw new IllegalArgumentException("Tried to add a duplicate comparator with the name:" + name);
		}
		SortEntry<T> sortEntry = new SortEntry<>(name, comparator, ingredientType);
		allSorters.put(name, sortEntry);
	}

	public void addUntypedComparison(ResourceLocation name, Comparator<ISortableIngredient<Object>> comparator) {
		if (allSorters.containsKey(name)) {
			throw new IllegalArgumentException("Tried to add a duplicate comparator with the name:" + name);
		}
		SortEntry<?> sortEntry = new SortEntry<>(name, comparator);
		allSorters.put(name, sortEntry);
	}

	public List<String> getSaveString() {
		return allSorters.values().stream().map(s -> s.getName().toString()).collect(Collectors.toList());
	}

	public void loadConfig(List<String> itemSortConfig) {
		chosenSorters = new ArrayList<>();
		if (itemSortConfig.isEmpty()) {
			chosenSorters.addAll(allSorters.values());
			return;
		}

		for (String savedListItem : itemSortConfig) {
			ResourceLocation resourceLocation = new ResourceLocation(savedListItem);
			SortEntry<?> sortEntry = allSorters.get(resourceLocation);
			if (sortEntry != null && !chosenSorters.contains(sortEntry)) {
				chosenSorters.add(sortEntry);
			}
		}
	}
}
