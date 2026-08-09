package mezz.jei.neoforge.platform;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IBrewingCategoryExtension;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

public class BrewingRecipeCategoryExtension implements IBrewingCategoryExtension<BrewingRecipe> {
	private final IIngredientHelper<ItemStack> itemStackHelper;

	public BrewingRecipeCategoryExtension(IIngredientHelper<ItemStack> itemStackHelper) {
		this.itemStackHelper = itemStackHelper;
	}

	@Override
	public List<IJeiBrewingRecipe> getBrewingRecipes(
		BrewingRecipe brewingRecipe,
		IVanillaRecipeFactory vanillaRecipeFactory
	) {
		List<ItemStack> ingredients = Arrays.stream(brewingRecipe.getIngredient().getItems())
			.filter(i -> !i.isEmpty())
			.toList();
		if (ingredients.isEmpty()) {
			return List.of();
		}

		Ingredient inputIngredient = brewingRecipe.getInput();
		List<ItemStack> inputs = Arrays.stream(inputIngredient.getItems())
			.filter(i -> !i.isEmpty())
			.toList();
		if (inputs.isEmpty()) {
			return List.of();
		}

		ItemStack output = brewingRecipe.getOutput();
		if (output.isEmpty()) {
			return List.of();
		}

		String outputModId = itemStackHelper.getResourceLocation(output).getNamespace();
		String uidPath = getUidPath(inputs, ingredients, output);
		IJeiBrewingRecipe recipe = vanillaRecipeFactory.createBrewingRecipe(
			ingredients,
			inputs,
			output,
			ResourceLocation.fromNamespaceAndPath(outputModId, uidPath)
		);
		return List.of(recipe);
	}

	private String getUidPath(List<ItemStack> inputs, List<ItemStack> ingredients, ItemStack output) {
		MessageDigest digest = createDigest();
		updateStackUids(digest, inputs);
		updateStackUids(digest, ingredients);
		updateString(digest, String.valueOf(itemStackHelper.getUid(output, UidContext.Recipe)));
		return "brewing/" + HexFormat.of().formatHex(digest.digest());
	}

	private void updateStackUids(MessageDigest digest, List<ItemStack> itemStacks) {
		List<String> stackUids = itemStacks.stream()
			.map(itemStack -> String.valueOf(itemStackHelper.getUid(itemStack, UidContext.Recipe)))
			.sorted()
			.toList();
		updateInt(digest, stackUids.size());
		stackUids.forEach(uid -> updateString(digest, uid));
	}

	private static void updateString(MessageDigest digest, String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		updateInt(digest, bytes.length);
		digest.update(bytes);
	}

	private static void updateInt(MessageDigest digest, int value) {
		digest.update((byte) (value >>> 24));
		digest.update((byte) (value >>> 16));
		digest.update((byte) (value >>> 8));
		digest.update((byte) value);
	}

	private static MessageDigest createDigest() {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is not available", e);
		}
	}
}
