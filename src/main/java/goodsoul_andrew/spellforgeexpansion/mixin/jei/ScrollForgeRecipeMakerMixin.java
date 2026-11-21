package goodsoul_andrew.spellforgeexpansion.mixin.jei;

import goodsoul_andrew.spellforgeexpansion.focus.FocusRequirement;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.jei.JeiPlugin;
import io.redspace.ironsspellbooks.jei.ScrollForgeRecipe;
import io.redspace.ironsspellbooks.jei.ScrollForgeRecipeMaker;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;

import java.lang.reflect.Field;
import java.util.*;

@Mixin(ScrollForgeRecipeMaker.class)
public class ScrollForgeRecipeMakerMixin {

    // Рефлексия для поля inkItems в JeiPlugin.ItemFinder
    private static final Field INK_ITEMS_FIELD;

    static {
        try {
            Class<?> itemFinderClass = Class.forName("io.redspace.ironsspellbooks.jei.JeiPlugin$ItemFinder");
            INK_ITEMS_FIELD = itemFinderClass.getDeclaredField("inkItems");
            INK_ITEMS_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access ItemFinder.inkItems", e);
        }
    }

    @Inject(method = "getRecipes", at = @At("HEAD"), cancellable = true)
    private static void spellforge$replaceRecipes(IVanillaRecipeFactory vanillaRecipeFactory,
                                                  @Coerce Object itemFinder,
                                                  CallbackInfoReturnable<List<ScrollForgeRecipe>> cir) {
        try {
            @SuppressWarnings("unchecked")
            var inkItems = (Collection<InkItem>) INK_ITEMS_FIELD.get(itemFinder);
            if (inkItems == null || inkItems.isEmpty()) {
                cir.setReturnValue(List.of()); // или fallback на оригинал
                return;
            }
            var sortedInks = new ArrayList<>(inkItems);
            sortedInks.sort(Comparator.comparingInt(i -> i.getRarity().ordinal()));
            var tagManager = ForgeRegistries.ITEMS.tags();
            if (tagManager == null) {
                return;
            }
            var paperInput = Ingredient.of(Items.PAPER);
            var recipes = new ArrayList<ScrollForgeRecipe>();
            for (var school : SchoolRegistry.REGISTRY.get()) {
                for (var ink : sortedInks) {
                    var inkRarity = ink.getRarity();
                    var tags = FocusRequirement.getTags(school, inkRarity);
                    List<ItemStack> focusStacks = new ArrayList<>();
                    boolean anyTagExists = false;
                    for (var tag : tags) {
                        if (tagManager.isKnownTagName(tag)) {
                            anyTagExists = true;
                            tagManager.getTag(tag).stream()
                                    .map(ItemStack::new)
                                    .forEach(focusStacks::add);
                        }
                    }
                    Ingredient focusInput;
                    if (anyTagExists) {
                        if (focusStacks.isEmpty()) {
                            continue;
                        }
                        focusInput = Ingredient.of(focusStacks.stream());
                    } else {
                        focusInput = Ingredient.of(school.getFocus());
                    }
                    var spells = SpellRegistry.getSpellsForSchool(school);
                    var scrollOutputs = new ArrayList<ItemStack>();
                    var inkOutputs = new ArrayList<ItemStack>();
                    for (var spell : spells) {
                        if (spell.isEnabled() && spell.allowCrafting()) {
                            var spellLevel = spell.getMinLevelForRarity(inkRarity);
                            if (spellLevel > 0 && spell != SpellRegistry.none()) {
                                inkOutputs.add(new ItemStack(ink));
                                scrollOutputs.add(getScrollStack(spell, spellLevel));
                            }
                        }
                    }
                    recipes.add(new ScrollForgeRecipe(inkOutputs, paperInput, focusInput, scrollOutputs));
                }
            }
            cir.setReturnValue(recipes);
        } catch (IllegalAccessException e) {
            // Логируем ошибку и возвращаем управление оригинальному методу
            goodsoul_andrew.spellforgeexpansion.SpellForgeExpansion.LOGGER.error(
                    "Failed to access ItemFinder.inkItems", e);
            // Не вызываем cir.setReturnValue, чтобы оригинал выполнился
        }
    }

    private static ItemStack getScrollStack(AbstractSpell spell, int spellLevel) {
        var scrollStack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell, spellLevel, scrollStack);
        return scrollStack;
    }
}
