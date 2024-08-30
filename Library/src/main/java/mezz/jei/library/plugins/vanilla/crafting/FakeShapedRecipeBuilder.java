package mezz.jei.library.plugins.vanilla.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FakeShapedRecipeBuilder {
	private final CraftingBookCategory category;
	private final ItemStack result;
	private final List<String> rows = Lists.newArrayList();
	private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
	@Nullable
	private String group;

	public FakeShapedRecipeBuilder(CraftingBookCategory category, ItemStack result) {
		this.category = category;
		this.result = result.copy();
	}

	public FakeShapedRecipeBuilder define(Character $$0, Ingredient $$1) {
		if (this.key.containsKey($$0)) {
			throw new IllegalArgumentException("Symbol '" + $$0 + "' is already defined!");
		} else if ($$0 == ' ') {
			throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
		} else {
			this.key.put($$0, $$1);
			return this;
		}
	}

	public FakeShapedRecipeBuilder pattern(String $$0) {
		if (!this.rows.isEmpty() && $$0.length() != this.rows.getFirst().length()) {
			throw new IllegalArgumentException("Pattern must be the same width on every line!");
		} else {
			this.rows.add($$0);
			return this;
		}
	}

	public FakeShapedRecipeBuilder group(@Nullable String $$0) {
		this.group = $$0;
		return this;
	}

	public ShapedRecipe build() {
		ShapedRecipePattern pattern = ShapedRecipePattern.of(this.key, this.rows);
		return new ShapedRecipe(
			Objects.requireNonNullElse(this.group, ""),
			category,
			pattern,
			result,
			false
		);
	}
}
