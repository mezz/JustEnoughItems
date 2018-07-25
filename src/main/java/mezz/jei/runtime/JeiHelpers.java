package mezz.jei.runtime;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IVanillaRecipeFactory;
import mezz.jei.gui.GuiHelper;
import mezz.jei.ingredients.IngredientBlacklist;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.plugins.vanilla.VanillaRecipeFactory;
import mezz.jei.startup.StackHelper;
import mezz.jei.transfer.RecipeTransferHandlerHelper;
import mezz.jei.util.Log;

public class JeiHelpers implements IJeiHelpers {
	private final GuiHelper guiHelper;
	private final StackHelper stackHelper;
	private final IngredientBlacklist ingredientBlacklist;
	private final RecipeTransferHandlerHelper recipeTransferHandlerHelper;
	private final IVanillaRecipeFactory vanillaRecipeFactory = new VanillaRecipeFactory();

	public JeiHelpers(IIngredientRegistry ingredientRegistry, IngredientBlacklistInternal ingredientBlacklistInternal, StackHelper stackHelper) {
		this.guiHelper = new GuiHelper(ingredientRegistry);
		this.stackHelper = stackHelper;
		this.ingredientBlacklist = new IngredientBlacklist(ingredientRegistry, ingredientBlacklistInternal);
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
	@Deprecated
	public IngredientBlacklist getItemBlacklist() {
		return ingredientBlacklist;
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

	@Override
	public void reload() {
		Log.get().error("A mod tried to reload JEI, this is no longer supported. See the javadocs for more information", new RuntimeException());
	}
}
