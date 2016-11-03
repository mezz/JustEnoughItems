package mezz.jei.gui.recipes;

import java.util.Arrays;
import java.util.List;

import net.minecraftforge.fml.client.config.HoverChecker;

public class RecipeClickableArea extends HoverChecker {
	private final List<String> recipeCategoryUids;

	public RecipeClickableArea(int top, int bottom, int left, int right, String... recipeCategoryUids) {
		super(top, bottom, left, right, 0);
		this.recipeCategoryUids = Arrays.asList(recipeCategoryUids);
	}

	public List<String> getRecipeCategoryUids() {
		return recipeCategoryUids;
	}
}
