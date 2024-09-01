package mezz.jei.library.plugins.vanilla.crafting;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mezz.jei.library.recipes.RecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JeiShapedRecipe extends ShapedRecipe {
	private final List<ItemStack> results;

	public JeiShapedRecipe(
		ResourceLocation id,
		String group,
		int width,
		int height,
		NonNullList<Ingredient> ingredients,
		List<ItemStack> results
	) {
		super(id, group, width, height, ingredients, results.get(0));
		this.results = results.stream()
			.map(ItemStack::copy)
			.toList();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializers.getJeiShapedRecipeSerializer();
	}

	@Override
	public ItemStack getResultItem() {
		return this.results.get(0);
	}

	@Override
	public ItemStack assemble(CraftingContainer craftingContainer) {
		return this.getResultItem().copy();
	}

	public List<ItemStack> getResults() {
		return this.results;
	}

	public static class Serializer implements RecipeSerializer<JeiShapedRecipe> {
		@Override
		public JeiShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			String group = GsonHelper.getAsString(json, "group", "");
			Map<Character, Ingredient> key = keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
			List<String> rows = patternFromJson(GsonHelper.getAsJsonArray(json, "pattern"));
			String[] pattern = JeiShapedRecipeBuilder.shrink(rows);
			if (pattern.length == 0) {
				throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
			}

			int width = pattern[0].length();
			int height = pattern.length;
			NonNullList<Ingredient> ingredients = JeiShapedRecipeBuilder.dissolvePattern(pattern, key, width, height);
			List<ItemStack> results = resultsFromJson(json.get("result"));
			return new JeiShapedRecipe(recipeId, group, width, height, ingredients, results);
		}

		@Nullable
		@Override
		public JeiShapedRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			int width = buffer.readVarInt();
			int height = buffer.readVarInt();
			String group = buffer.readUtf();
			NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);

			for (int i = 0; i < ingredients.size(); i++) {
				ingredients.set(i, Ingredient.fromNetwork(buffer));
			}

			int resultCount = buffer.readVarInt();
			List<ItemStack> results = new ArrayList<>(resultCount);
			for (int i = 0; i < resultCount; i++) {
				results.add(buffer.readItem());
			}
			return new JeiShapedRecipe(recipeId, group, width, height, ingredients, results);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, JeiShapedRecipe recipe) {
			buffer.writeVarInt(recipe.getWidth());
			buffer.writeVarInt(recipe.getHeight());
			buffer.writeUtf(recipe.getGroup());

			for (Ingredient ingredient : recipe.getIngredients()) {
				ingredient.toNetwork(buffer);
			}

			List<ItemStack> results = recipe.getResults();
			buffer.writeVarInt(results.size());
			for (ItemStack result : results) {
				buffer.writeItem(result);
			}
		}

		private static List<String> patternFromJson(JsonArray patternArray) {
			String[] pattern = new String[patternArray.size()];
			if (pattern.length > 3) {
				throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
			} else if (pattern.length == 0) {
				throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
			}

			for (int i = 0; i < pattern.length; i++) {
				String row = GsonHelper.convertToString(patternArray.get(i), "pattern[" + i + "]");
				if (row.length() > 3) {
					throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
				}

				if (i > 0 && pattern[0].length() != row.length()) {
					throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
				}

				pattern[i] = row;
			}

			return List.of(pattern);
		}

		private static Map<Character, Ingredient> keyFromJson(JsonObject keyEntry) {
			Map<Character, Ingredient> key = Maps.newHashMap();

			for (Map.Entry<String, JsonElement> entry : keyEntry.entrySet()) {
				String symbol = entry.getKey();
				if (symbol.length() != 1) {
					throw new JsonSyntaxException("Invalid key entry: '" + symbol + "' is an invalid symbol (must be 1 character only).");
				}

				if (" ".equals(symbol)) {
					throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
				}

				key.put(symbol.charAt(0), Ingredient.fromJson(entry.getValue()));
			}

			return key;
		}

		private static List<ItemStack> resultsFromJson(@Nullable JsonElement resultElement) {
			if (resultElement == null) {
				throw new JsonSyntaxException("Missing result, expected to find an object or array");
			}

			if (resultElement.isJsonArray()) {
				JsonArray resultArray = resultElement.getAsJsonArray();
				if (resultArray.isEmpty()) {
					throw new JsonSyntaxException("Invalid result: empty result array not allowed");
				}

				List<ItemStack> results = new ArrayList<>(resultArray.size());
				for (JsonElement element : resultArray) {
					JsonObject resultObject = GsonHelper.convertToJsonObject(element, "result");
					results.add(ShapedRecipe.itemStackFromJson(resultObject));
				}
				return results;
			}

			JsonObject resultObject = GsonHelper.convertToJsonObject(resultElement, "result");
			return List.of(ShapedRecipe.itemStackFromJson(resultObject));
		}
	}
}
