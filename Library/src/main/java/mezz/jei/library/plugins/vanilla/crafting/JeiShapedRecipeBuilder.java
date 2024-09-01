package mezz.jei.library.plugins.vanilla.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.recipe.vanilla.IJeiShapedRecipeBuilder;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class JeiShapedRecipeBuilder implements IJeiShapedRecipeBuilder {
	private static final AtomicInteger NEXT_GENERATED_ID = new AtomicInteger();

	private final @Nullable ResourceLocation id;
	private final CraftingBookCategory category;
	private final List<ItemStack> results;
	private final List<String> rows = Lists.newArrayList();
	private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
	private String group = "";

	public JeiShapedRecipeBuilder(CraftingBookCategory category, List<ItemStack> results) {
		this(null, category, results);
	}

	public JeiShapedRecipeBuilder(@Nullable ResourceLocation id, CraftingBookCategory category, List<ItemStack> results) {
		ErrorUtil.checkNotNull(category, "category");
		ErrorUtil.checkNotEmpty(results, "results");

		this.id = id;
		this.category = category;
		this.results = results.stream()
			.map(ItemStack::copy)
			.toList();
	}

	@Override
	public JeiShapedRecipeBuilder define(Character character, Ingredient ingredient) {
		ErrorUtil.checkNotNull(character, "character");
		ErrorUtil.checkNotNull(ingredient, "ingredient");

		if (this.key.containsKey(character)) {
			throw new IllegalArgumentException("Symbol '" + character + "' is already defined!");
		} else if (character == ' ') {
			throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
		} else {
			this.key.put(character, ingredient);
			return this;
		}
	}

	@Override
	public JeiShapedRecipeBuilder pattern(String patternRow) {
		ErrorUtil.checkNotNull(patternRow, "patternRow");

		if (patternRow.isEmpty()) {
			throw new IllegalArgumentException("Pattern row cannot be empty");
		} else if (patternRow.length() > 3) {
			throw new IllegalArgumentException("Invalid pattern: too many columns, 3 is maximum");
		} else if (this.rows.size() >= 3) {
			throw new IllegalArgumentException("Invalid pattern: too many rows, 3 is maximum");
		} else if (!this.rows.isEmpty() && patternRow.length() != this.rows.get(0).length()) {
			throw new IllegalArgumentException("Pattern must be the same width on every line!");
		} else {
			this.rows.add(patternRow);
			return this;
		}
	}

	@Override
	public JeiShapedRecipeBuilder group(String group) {
		this.group = Objects.requireNonNullElse(group, "");
		return this;
	}

	@Override
	public JeiShapedRecipe build() {
		String[] pattern = shrink(this.rows);
		if (pattern.length == 0) {
			throw new IllegalArgumentException("Invalid pattern: empty pattern not allowed");
		}

		int width = pattern[0].length();
		int height = pattern.length;
		NonNullList<Ingredient> inputs = dissolvePattern(pattern, this.key, width, height);
		ResourceLocation recipeId = Objects.requireNonNullElseGet(this.id, JeiShapedRecipeBuilder::createGeneratedId);

		return new JeiShapedRecipe(recipeId, this.group, this.category, width, height, inputs, this.results);
	}

	private static ResourceLocation createGeneratedId() {
		return new ResourceLocation(ModIds.JEI_ID, "jei_shaped/" + NEXT_GENERATED_ID.getAndIncrement());
	}

	static NonNullList<Ingredient> dissolvePattern(String[] pattern, Map<Character, Ingredient> keys, int patternWidth, int patternHeight) {
		NonNullList<Ingredient> ingredients = NonNullList.withSize(patternWidth * patternHeight, Ingredient.EMPTY);
		Set<Character> unusedKeys = Sets.newHashSet(keys.keySet());

		for (int row = 0; row < pattern.length; row++) {
			for (int column = 0; column < pattern[row].length(); column++) {
				char symbol = pattern[row].charAt(column);
				Ingredient ingredient = symbol == ' ' ? Ingredient.EMPTY : keys.get(symbol);
				if (ingredient == null) {
					throw new IllegalArgumentException("Pattern references symbol '" + symbol + "' but it's not defined in the key");
				}

				unusedKeys.remove(symbol);
				ingredients.set(column + patternWidth * row, ingredient);
			}
		}

		if (!unusedKeys.isEmpty()) {
			throw new IllegalArgumentException("Key defines symbols that aren't used in pattern: " + unusedKeys);
		}

		return ingredients;
	}

	static String[] shrink(List<String> rows) {
		int firstColumn = Integer.MAX_VALUE;
		int lastColumn = 0;
		int firstRow = 0;
		int trailingEmptyRows = 0;

		for (int row = 0; row < rows.size(); row++) {
			String patternRow = rows.get(row);
			firstColumn = Math.min(firstColumn, firstNonSpace(patternRow));
			int rowLastColumn = lastNonSpace(patternRow);
			lastColumn = Math.max(lastColumn, rowLastColumn);
			if (rowLastColumn < 0) {
				if (firstRow == row) {
					firstRow++;
				}

				trailingEmptyRows++;
			} else {
				trailingEmptyRows = 0;
			}
		}

		if (rows.size() == trailingEmptyRows) {
			return new String[0];
		}

		String[] pattern = new String[rows.size() - trailingEmptyRows - firstRow];
		for (int row = 0; row < pattern.length; row++) {
			pattern[row] = rows.get(row + firstRow).substring(firstColumn, lastColumn + 1);
		}
		return pattern;
	}

	private static int firstNonSpace(String row) {
		int column = 0;
		while (column < row.length() && row.charAt(column) == ' ') {
			column++;
		}
		return column;
	}

	private static int lastNonSpace(String row) {
		int column = row.length() - 1;
		while (column >= 0 && row.charAt(column) == ' ') {
			column--;
		}
		return column;
	}
}
