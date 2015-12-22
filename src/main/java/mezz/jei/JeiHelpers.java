package mezz.jei;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.JEIManager;
import mezz.jei.gui.GuiHelper;
import mezz.jei.transfer.RecipeTransferHandlerHelper;

public class JeiHelpers implements IJeiHelpers {
	private final GuiHelper guiHelper;
	private final ItemBlacklist itemBlacklist;
	private final NbtIgnoreList nbtIgnoreList;
	private final RecipeTransferHandlerHelper recipeTransferHandlerHelper;

	public JeiHelpers() {
		this.guiHelper = new GuiHelper();
		this.itemBlacklist = new ItemBlacklist();
		this.nbtIgnoreList = new NbtIgnoreList();
		this.recipeTransferHandlerHelper = new RecipeTransferHandlerHelper();

		JEIManager.guiHelper = this.guiHelper;
		JEIManager.itemBlacklist = this.itemBlacklist;
		JEIManager.nbtIgnoreList = this.nbtIgnoreList;
	}

	@Override
	public GuiHelper getGuiHelper() {
		return guiHelper;
	}

	@Override
	public ItemBlacklist getItemBlacklist() {
		return itemBlacklist;
	}

	@Override
	public NbtIgnoreList getNbtIgnoreList() {
		return nbtIgnoreList;
	}

	@Override
	public RecipeTransferHandlerHelper recipeTransferHandlerHelper() {
		return recipeTransferHandlerHelper;
	}
}
