package mezz.jei.vote;

import mezz.jei.api.ingredients.IIngredientHelper;

import javax.annotation.Nullable;

import mezz.jei.api.constants.ModIds;

public class GoVoteIngredientHelper implements IIngredientHelper<GoVoteIngredient> {
	@Nullable
	@Override
	public GoVoteIngredient getMatch(Iterable<GoVoteIngredient> ingredients, GoVoteIngredient ingredientToMatch) {
		for (GoVoteIngredient ingredient : ingredients) {
			if (ingredient.getUid().equals(ingredientToMatch.getUid())) {
				return ingredient;
			}
		}
		return null;
	}

	@Override
	public String getDisplayName(GoVoteIngredient ingredient) {
		return ingredient.getDisplayName();
	}

	@Override
	public String getUniqueId(GoVoteIngredient ingredient) {
		return ModIds.JEI_ID + "_vote_" + ingredient.getUid();
	}

	@Override
	public String getModId(GoVoteIngredient ingredient) {
		return ModIds.JEI_ID;
	}

	@Override
	public String getResourceId(GoVoteIngredient ingredient) {
		return ModIds.JEI_ID;
	}

	@Override
	public GoVoteIngredient copyIngredient(GoVoteIngredient ingredient) {
		return ingredient;
	}

	@Override
	public String getErrorInfo(@Nullable GoVoteIngredient ingredient) {
		if (ingredient == null) {
			return "null";
		}
		return ingredient.toString();
	}
}
