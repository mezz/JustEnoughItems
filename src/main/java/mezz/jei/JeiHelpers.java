package mezz.jei;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.gui.GuiHelper;
import mezz.jei.transfer.RecipeTransferHandlerHelper;
import mezz.jei.util.StackHelper;

public class JeiHelpers implements IJeiHelpers {
	private final GuiHelper guiHelper;
	private final StackHelper stackHelper;
	private final ItemBlacklist itemBlacklist;
	private final RecipeTransferHandlerHelper recipeTransferHandlerHelper;

	public JeiHelpers(IIngredientRegistry ingredientRegistry, StackHelper stackHelper) {
		this.guiHelper = new GuiHelper();
		this.stackHelper = stackHelper;
		this.itemBlacklist = new ItemBlacklist(ingredientRegistry);
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
	public ItemBlacklist getItemBlacklist() {
		return itemBlacklist;
	}

	@Override
	public RecipeTransferHandlerHelper recipeTransferHandlerHelper() {
		return recipeTransferHandlerHelper;
	}

	@Override
	public void reload() {
		JustEnoughItems.getProxy().restartJEI();
	}
}
