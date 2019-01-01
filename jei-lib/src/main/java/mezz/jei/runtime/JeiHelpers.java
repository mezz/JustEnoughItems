package mezz.jei.runtime;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IVanillaRecipeFactory;
import mezz.jei.config.IHideModeConfig;
import mezz.jei.gui.GuiHelper;
import mezz.jei.ingredients.IngredientBlacklist;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.startup.StackHelper;
import mezz.jei.transfer.RecipeTransferHandlerHelper;

public class JeiHelpers implements IJeiHelpers {
	private final GuiHelper guiHelper;
	private final StackHelper stackHelper;
	private final IngredientBlacklist ingredientBlacklist;
	private final RecipeTransferHandlerHelper recipeTransferHandlerHelper;
	private final IVanillaRecipeFactory vanillaRecipeFactory = new VanillaRecipeFactory();

	public JeiHelpers(IIngredientRegistry ingredientRegistry, IngredientBlacklistInternal ingredientBlacklistInternal, StackHelper stackHelper, IHideModeConfig hideModeConfig) {
		this.guiHelper = new GuiHelper(ingredientRegistry);
		this.stackHelper = stackHelper;
		this.ingredientBlacklist = new IngredientBlacklist(ingredientRegistry, ingredientBlacklistInternal, hideModeConfig);
		this.recipeTransferHandlerHelper = new RecipeTransferHandlerHelper();
	}

	@Override
	public GuiHelper getGuiHelper() {
		return guiHelper;
	}

	@Override
	public StackHelper getStackHelper() {
		return stackHelper;
	}

	@Override
	public IngredientBlacklist getIngredientBlacklist() {
		return ingredientBlacklist;
	}

	@Override
	public RecipeTransferHandlerHelper recipeTransferHandlerHelper() {
		return recipeTransferHandlerHelper;
	}

	@Override
	public IVanillaRecipeFactory getVanillaRecipeFactory() {
		return vanillaRecipeFactory;
	}
}
