package mezz.jei;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.INbtRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.gui.GuiHelper;
import mezz.jei.transfer.RecipeTransferHandlerHelper;
import mezz.jei.util.StackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class JeiHelpers implements IJeiHelpers {
	@Nonnull
	private final GuiHelper guiHelper;
	@Nonnull
	private final StackHelper stackHelper;
	@Nonnull
	private final ItemBlacklist itemBlacklist;
	@Nonnull
	private final NbtIgnoreList nbtIgnoreList;
	@Nonnull
	private final SubtypeRegistry subtypeRegistry;
	@Nonnull
	private final RecipeTransferHandlerHelper recipeTransferHandlerHelper;

	public JeiHelpers() {
		this.guiHelper = new GuiHelper();
		this.stackHelper = new StackHelper();
		this.itemBlacklist = new ItemBlacklist();
		this.nbtIgnoreList = new NbtIgnoreList();
		this.subtypeRegistry = new SubtypeRegistry();
		this.recipeTransferHandlerHelper = new RecipeTransferHandlerHelper();
	}

	@Nonnull
	@Override
	public GuiHelper getGuiHelper() {
		return guiHelper;
	}

	@Nonnull
	@Override
	public StackHelper getStackHelper() {
		return stackHelper;
	}

	@Nonnull
	@Override
	public ItemBlacklist getItemBlacklist() {
		return itemBlacklist;
	}

	@Nonnull
	@Override
	@Deprecated
	public NbtIgnoreList getNbtIgnoreList() {
		return nbtIgnoreList;
	}

	@Override
	public ISubtypeRegistry getSubtypeRegistry() {
		return subtypeRegistry;
	}

	@Override
	public INbtRegistry getNbtRegistry() {
		return new INbtRegistry() {
			@Override
			public void useNbtForSubtypes(@Nonnull Item... items) {

			}

			@Override
			public void registerNbtInterpreter(@Nonnull Item item, @Nonnull INbtInterpreter nbtInterpreter) {

			}

			@Nullable
			@Override
			public String getSubtypeInfoFromNbt(@Nonnull ItemStack itemStack) {
				return null;
			}
		};
	}

	@Nonnull
	@Override
	public RecipeTransferHandlerHelper recipeTransferHandlerHelper() {
		return recipeTransferHandlerHelper;
	}

	@Override
	public void reload() {
		JustEnoughItems.getProxy().restartJEI();
	}
}
