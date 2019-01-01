package mezz.jei.gui.recipes;

import java.util.Arrays;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import mezz.jei.gui.HoverChecker;

public class RecipeClickableArea extends HoverChecker {
	private final List<ResourceLocation> recipeCategoryUids;

	public RecipeClickableArea(int top, int bottom, int left, int right, ResourceLocation... recipeCategoryUids) {
		super(top, bottom, left, right);
		this.recipeCategoryUids = Arrays.asList(recipeCategoryUids);
	}

	public List<ResourceLocation> getRecipeCategoryUids() {
		return recipeCategoryUids;
	}
}
