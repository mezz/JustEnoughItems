package mezz.jei;

import javax.annotation.Nullable;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.INbtRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.gui.GuiHelper;
import mezz.jei.transfer.RecipeTransferHandlerHelper;
import mezz.jei.util.ModIdUtil;
import mezz.jei.util.StackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class JeiHelpers implements IJeiHelpers {
	private final GuiHelper guiHelper;
	private final StackHelper stackHelper;
	private final ItemBlacklist itemBlacklist;
	private final NbtIgnoreList nbtIgnoreList;
	private final SubtypeRegistry subtypeRegistry;
	private final RecipeTransferHandlerHelper recipeTransferHandlerHelper;
	private final ModIdUtil modIdUtil;

	public JeiHelpers() {
		this.guiHelper = new GuiHelper();
		this.stackHelper = new StackHelper();
		this.itemBlacklist = new ItemBlacklist();
		this.nbtIgnoreList = new NbtIgnoreList();
		this.subtypeRegistry = new SubtypeRegistry();
		this.recipeTransferHandlerHelper = new RecipeTransferHandlerHelper();
		this.modIdUtil = new ModIdUtil();
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
	public NbtIgnoreList getNbtIgnoreList() {
		return nbtIgnoreList;
	}

	@Override
	public ISubtypeRegistry getSubtypeRegistry() {
		return subtypeRegistry;
	}

	public ModIdUtil getModIdUtil() {
		return modIdUtil;
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
