package mezz.jei;

import javax.annotation.Nullable;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.INbtRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.gui.GuiHelper;
import mezz.jei.transfer.RecipeTransferHandlerHelper;
import mezz.jei.util.StackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class JeiHelpers implements IJeiHelpers {
	private final GuiHelper guiHelper;
	private final StackHelper stackHelper;
    private final IngredientBlacklist ingredientBlacklist;
	private final ItemBlacklist itemBlacklist;
	private final NbtIgnoreList nbtIgnoreList;
	private final SubtypeRegistry subtypeRegistry;
	private final RecipeTransferHandlerHelper recipeTransferHandlerHelper;

	public JeiHelpers(IIngredientRegistry ingredientRegistry, StackHelper stackHelper, SubtypeRegistry subtypeRegistry) {
		this.guiHelper = new GuiHelper(stackHelper);
		this.stackHelper = stackHelper;
        this.ingredientBlacklist = new IngredientBlacklist(ingredientRegistry);
		this.itemBlacklist = new ItemBlacklist(ingredientBlacklist);
		this.nbtIgnoreList = new NbtIgnoreList();
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
	@Deprecated
	public ItemBlacklist getItemBlacklist() {
		return itemBlacklist;
	}
	
	@Override
	public IngredientBlacklist getIngredientBlacklist() {
	    return ingredientBlacklist;
	}

	@Override
	@Deprecated
	public NbtIgnoreList getNbtIgnoreList() {
		return nbtIgnoreList;
	}

	@Override
	@Deprecated
	public ISubtypeRegistry getSubtypeRegistry() {
		return subtypeRegistry;
	}

	@Override
	public INbtRegistry getNbtRegistry() {
		return new INbtRegistry() {
			@Override
			public void useNbtForSubtypes(Item... items) {

			}

			@Override
			public void registerNbtInterpreter(Item item, INbtInterpreter nbtInterpreter) {

			}

			@Nullable
			@Override
			public String getSubtypeInfoFromNbt(ItemStack itemStack) {
				return null;
			}
		};
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
