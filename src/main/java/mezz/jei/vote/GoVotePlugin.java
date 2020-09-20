package mezz.jei.vote;

import mezz.jei.Internal;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@JeiPlugin
public class GoVotePlugin implements IModPlugin {
	@Nullable
	private static GoVoteIngredient voteIngredient;

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ModIds.JEI_ID, "vote");
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		Minecraft minecraft = Minecraft.getInstance();
		if (isGoVoteEnabled(minecraft)) {
			GoVoteIngredient voteIngredient = getVoteIngredient(minecraft);
			GoVoteIngredient bidenIngredient = new GoVoteIngredient(() -> Internal.getTextures().getBiden(), "biden", "Support Joe Biden", Arrays.asList(
				new StringTextComponent("If you are eligible for the 2020 US election,"),
				new StringTextComponent("click here to Support Joe Biden's Campaign!")
			), "https://joebiden.com/");
			List<GoVoteIngredient> ingredients = Arrays.asList(voteIngredient, bidenIngredient);
			registration.register(GoVoteIngredient.TYPE, ingredients, new GoVoteIngredientHelper(), new GoVoteIngredientRenderer());
		}
	}

	public static boolean isGoVoteEnabled(Minecraft minecraft) {
		return !GoVoteHandler.isAfterElectionDay() && GoVoteHandler.isProbablyUsaLocale(minecraft);
	}

	@Nullable
	public static GoVoteIngredient getVoteIngredient(Minecraft minecraft) {
		if (isGoVoteEnabled(minecraft)) {
			if (voteIngredient == null) {
				voteIngredient = new GoVoteIngredient(() -> Internal.getTextures().getVote(), "vote", "Register to Vote", Arrays.asList(
					new StringTextComponent("If you are eligible for the 2020 US election,"),
					new StringTextComponent("click here to easily Register to Vote"),
					new StringTextComponent("or to verify your registration status!")
				), GoVoteHandler.VOTE_ORG_LINK);
			}
			return voteIngredient;
		}
		return null;
	}
}
