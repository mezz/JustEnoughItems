package mezz.jei.common.ingredients.group;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.Identifier;

public enum IngredientGroupType {

	INGREDIENTS {
		@Override
		public MapCodec<IngredientsSelector> getCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager) {
			return codecHelper.getTypedIngredientCodec()
							  .codec()
							  .listOf()
							  .xmap(
									  IngredientsSelector::new,
									  IngredientsSelector::ingredients
							  ).fieldOf("ingredients");
		}
	},
	TAG {
		@Override
		public MapCodec<TagSelector> getCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager) {
			return Identifier.CODEC
					.xmap(TagSelector::new, TagSelector::tagId)
					.fieldOf("tag");
		}
	},
	REGEXP {
		@Override
		public MapCodec<RegExpSelector> getCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager) {
			return Codec.STRING
					.xmap(RegExpSelector::new, RegExpSelector::pattern)
					.fieldOf("pattern");
		}
	},
	DYNAMIC {
		@Override
		public MapCodec<? extends IIngredientGroupSelector> getCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager) {
			throw new UnsupportedOperationException("DynamicSelector cannot be serialized");
		}
	};

	public abstract MapCodec<? extends IIngredientGroupSelector> getCodec(ICodecHelper codecHelper, IIngredientManager ingredientManager);

}

