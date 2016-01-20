package mezz.jei.gui;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import net.minecraftforge.fml.client.config.HoverChecker;

public class RecipeClickableArea extends HoverChecker {
	@Nonnull
	private final List<String> recipeCategoryUids;

	public RecipeClickableArea(int top, int bottom, int left, int right, @Nonnull String... recipeCategoryUids) {
		super(top, bottom, left, right, 0);
		this.recipeCategoryUids = Arrays.asList(recipeCategoryUids);
	}

	@Nonnull
	public List<String> getRecipeCategoryUids() {
		return recipeCategoryUids;
	}
}
