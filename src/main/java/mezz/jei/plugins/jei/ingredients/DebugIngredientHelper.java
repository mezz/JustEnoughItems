package mezz.jei.plugins.jei.ingredients;

import javax.annotation.Nullable;

import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.util.CommandUtilServer;

public class DebugIngredientHelper implements IIngredientHelper<DebugIngredient> {
	@Nullable
	@Override
	public DebugIngredient getMatch(Iterable<DebugIngredient> ingredients, DebugIngredient ingredientToMatch, UidContext context) {
		for (DebugIngredient debugIngredient : ingredients) {
			if (debugIngredient.getNumber() == ingredientToMatch.getNumber()) {
				String keyLhs = getUniqueId(ingredientToMatch, context);
				String keyRhs = getUniqueId(debugIngredient, context);
				if (keyLhs.equals(keyRhs)) {
					return debugIngredient;
				}
			}
		}
		return null;
	}

	@Override
	public String getDisplayName(DebugIngredient ingredient) {
		return "JEI Debug Item #" + ingredient.getNumber();
	}

	@Override
	public String getUniqueId(DebugIngredient ingredient, UidContext context) {
		return "JEI_debug_" + ingredient.getNumber();
	}

	@Override
	public String getModId(DebugIngredient ingredient) {
		return ModIds.JEI_ID;
	}

	@Override
	public String getResourceId(DebugIngredient ingredient) {
		return "debug_" + ingredient.getNumber();
	}

	@Override
	public ItemStack getCheatItemStack(DebugIngredient ingredient) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player != null) {
			CommandUtilServer.writeChatMessage(player, "Debug ingredients cannot be cheated", TextFormatting.RED);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public DebugIngredient copyIngredient(DebugIngredient ingredient) {
		return ingredient.copy();
	}

	@Override
	public String getErrorInfo(@Nullable DebugIngredient ingredient) {
		if (ingredient == null) {
			return "debug ingredient: null";
		}
		return getDisplayName(ingredient);
	}
}
