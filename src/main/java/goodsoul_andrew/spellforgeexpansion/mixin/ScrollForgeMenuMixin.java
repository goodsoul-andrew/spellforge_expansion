package goodsoul_andrew.spellforgeexpansion.mixin;

import goodsoul_andrew.spellforgeexpansion.focus.FocusRequirement;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeMenu;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScrollForgeMenu.class)
public abstract class ScrollForgeMenuMixin {

    @Shadow
    private AbstractSpell spellRecipeSelection;

    @Shadow
    public abstract Slot getBlankScrollSlot();

    @Shadow
    public abstract Slot getInkSlot();

    @Shadow
    public abstract Slot getFocusSlot();

    @Shadow
    public abstract Slot getResultSlot();

    @Inject(
            method = "setupResultSlot",
            at = @At("HEAD"),
            cancellable = true
    )
    private void spellforge_expansion$setupResultSlot(AbstractSpell spell, CallbackInfo ci) {
        ItemStack scrollStack = this.getBlankScrollSlot().getItem();
        ItemStack inkStack = this.getInkSlot().getItem();
        ItemStack focusStack = this.getFocusSlot().getItem();
        ItemStack resultStack = ItemStack.EMPTY;

        if (!scrollStack.isEmpty() && !inkStack.isEmpty() && !focusStack.isEmpty()
                && spell != null && !spell.equals(SpellRegistry.none())
                && spell.allowCrafting()
                && inkStack.getItem() instanceof InkItem inkItem
                && FocusRequirement.accepts(focusStack, spell, inkItem.getRarity())) {
            if (scrollStack.getItem().equals(Items.PAPER)) {
                resultStack = new ItemStack(ItemRegistry.SCROLL.get());
                resultStack.setCount(1);
                ISpellContainer.createScrollContainer(spell, spell.getMinLevelForRarity(inkItem.getRarity()), resultStack);
            }
        }

        if (!ItemStack.matches(resultStack, this.getResultSlot().getItem())) {
            if (resultStack.isEmpty()) {
                this.spellRecipeSelection = SpellRegistry.none();
            }
            this.getResultSlot().set(resultStack);
        }

        ci.cancel();
    }
}
