package mezz.jei.api.constants;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Built-in {@link IIngredientType} for vanilla Minecraft.
 */
public final class VanillaTypes {
	/**
	 * @since 9.7.0
	 */
	public static final IIngredientTypeWithSubtypes<Item, ItemStack> ITEM_STACK = new IIngredientTypeWithSubtypes<>() {
		@Override
		public String getUid() {
			return "item_stack";
		}

		@Override
		public Class<? extends ItemStack> getIngredientClass() {
			return ItemStack.class;
		}

		@Override
		public Class<? extends Item> getIngredientBaseClass() {
			return Item.class;
		}

		@Override
		public Item getBase(ItemStack ingredient) {
			return ingredient.getItem();
		}

		@Override
		public ItemStack getDefaultIngredient(Item base) {
			return base.getDefaultInstance();
		}
	};

	private VanillaTypes() {

	}
}
