package mezz.jei;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.gui.GuiHelper;
import mezz.jei.transfer.RecipeTransferHandlerHelper;
import mezz.jei.util.StackHelper;

public class JeiHelpers implements IJeiHelpers {
	private final GuiHelper guiHelper;
	private final StackHelper stackHelper;
	private final ItemBlacklist itemBlacklist;
	private final SubtypeRegistry subtypeRegistry;
	private final RecipeTransferHandlerHelper recipeTransferHandlerHelper;

	public JeiHelpers(IIngredientRegistry ingredientRegistry, StackHelper stackHelper, SubtypeRegistry subtypeRegistry) {
		this.guiHelper = new GuiHelper(stackHelper);
		this.stackHelper = stackHelper;
		this.itemBlacklist = new ItemBlacklist(ingredientRegistry);
		this.subtypeRegistry = subtypeRegistry;
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
	@Deprecated
	public ISubtypeRegistry getSubtypeRegistry() {
		return subtypeRegistry;
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
