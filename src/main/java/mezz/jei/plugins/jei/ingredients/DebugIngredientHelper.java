package mezz.jei.plugins.jei.ingredients;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.util.CommandUtilServer;

public class DebugIngredientHelper implements IIngredientHelper<DebugIngredient> {
	@Override
	public IIngredientType<DebugIngredient> getIngredientType() {
		return DebugIngredient.TYPE;
	}

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
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			CommandUtilServer.writeChatMessage(player, "Debug ingredients cannot be cheated", ChatFormatting.RED);
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
