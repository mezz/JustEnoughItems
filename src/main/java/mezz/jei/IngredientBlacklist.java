package mezz.jei;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mezz.jei.api.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.util.IngredientUtil;
import mezz.jei.util.Log;

public class IngredientBlacklist implements IIngredientBlacklist {
    private final IIngredientRegistry ingredientRegistry;
    private final Set<String> ingredientBlacklist = new HashSet<String>();
    
    public IngredientBlacklist(IIngredientRegistry ingredientRegistry) {
        this.ingredientRegistry = ingredientRegistry;
    }
    
    @Override
    public <V> void addIngredientToBlacklist(V ingredient) {
        if (ingredient == null) {
            Log.error("Null ingredient", new NullPointerException());
            return;
        }
        
        String uniqueName = ingredientRegistry.getIngredientHelper(ingredient).getUniqueId(ingredient);
        ingredientBlacklist.add(uniqueName);
    }

    @Override
    public <V> void removeIngredientFromBlacklist(V ingredient) {
        if (ingredient == null) {
            Log.error("Null ingredient", new NullPointerException());
            return;
        }
        
        String uniqueName = ingredientRegistry.getIngredientHelper(ingredient).getUniqueId(ingredient);
        ingredientBlacklist.remove(uniqueName);
    }

    @Override
    public <V> boolean isIngredientBlacklisted(V ingredient) {
        if (ingredient == null) {
            Log.error("Null ingredient", new NullPointerException());
            return true;
        }
        
        return isIngredientBlacklistedByApi(ingredient) || Config.isIngredientOnConfigBlacklist(ingredient, ingredientRegistry.getIngredientHelper(ingredient));
    }
    
    public <V> boolean isIngredientBlacklistedByApi(V ingredient) {
        List<String> uids = IngredientUtil.getUniqueIdsWithWildcard(ingredientRegistry.getIngredientHelper(ingredient), ingredient);
        
        for (String uid : uids) {
            if (ingredientBlacklist.contains(uid)) {
                return true;
            }
        }
        
        return false;
    }
}
