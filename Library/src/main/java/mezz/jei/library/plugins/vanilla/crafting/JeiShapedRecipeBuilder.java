package mezz.jei.library.plugins.vanilla.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import mezz.jei.api.recipe.vanilla.IJeiShapedRecipeBuilder;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JeiShapedRecipeBuilder implements IJeiShapedRecipeBuilder {
	private final CraftingBookCategory category;
	private final SlotDisplay results;
	private final List<String> rows = Lists.newArrayList();
	private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
	private final Map<Character, SlotDisplay> displayKey = Maps.newLinkedHashMap();
	private String group = "";

	public JeiShapedRecipeBuilder(CraftingBookCategory category, SlotDisplay results) {
		this.category = category;
		this.results = results;
	}

	@Override
	public JeiShapedRecipeBuilder define(Character character, Ingredient ingredient) {
		if (this.key.containsKey(character)) {
			throw new IllegalArgumentException("Symbol '" + character + "' is already defined!");
		} else if (character == ' ') {
			throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
		} else {
			this.key.put(character, ingredient);
			this.displayKey.put(character, ingredient.display());
			return this;
		}
	}

	@Override
	public IJeiShapedRecipeBuilder define(Character character, Ingredient ingredient, SlotDisplay display) {
		if (this.key.containsKey(character)) {
			throw new IllegalArgumentException("Symbol '" + character + "' is already defined!");
		} else if (character == ' ') {
			throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
		} else {
			this.key.put(character, ingredient);
			this.displayKey.put(character, display);
			return this;
		}
	}

	@Override
	public JeiShapedRecipeBuilder pattern(String patternRow) {
		if (!this.rows.isEmpty() && patternRow.length() != this.rows.getFirst().length()) {
			throw new IllegalArgumentException("Pattern must be the same width on every line!");
		} else {
			this.rows.add(patternRow);
			return this;
		}
	}

	@Override
	public JeiShapedRecipeBuilder group(String $$0) {
		this.group = $$0;
		return this;
	}

	@Override
	public CraftingRecipe build() {
		ShapedRecipePattern pattern = ShapedRecipePattern.of(this.key, this.rows);
		List<SlotDisplay> displays = unpack(this.displayKey, this.rows).getOrThrow();
		return new JeiShapedRecipe(
			group,
			category,
			pattern,
			displays,
			results
		);
	}

	private static DataResult<List<SlotDisplay>> unpack(Map<Character, SlotDisplay> key, List<String> pattern) {
		IPlatformRecipeHelper recipeHelper = Services.PLATFORM.getRecipeHelper();
		String[] shrunkPattern = recipeHelper.shrinkShapedRecipePattern(pattern);
		int i = shrunkPattern[0].length();
		int j = shrunkPattern.length;
		List<SlotDisplay> list = new ArrayList<>(i * j);
		CharSet charset = new CharArraySet(key.keySet());

		for (String s : shrunkPattern) {
			for (int k = 0; k < s.length(); k++) {
				char c = s.charAt(k);
				final SlotDisplay slotDisplay;
				if (c == ' ') {
					slotDisplay = SlotDisplay.Empty.INSTANCE;
				} else {
					slotDisplay = key.get(c);
					if (slotDisplay == null) {
						return DataResult.error(() -> "Pattern references symbol '" + c + "' but it's not defined in the key");
					}
				}

				charset.remove(c);
				list.add(slotDisplay);
			}
		}

		if (!charset.isEmpty()) {
			return DataResult.error(() -> "Key defines symbols that aren't used in pattern: " + charset);
		}
		return DataResult.success(list);
	}
}
