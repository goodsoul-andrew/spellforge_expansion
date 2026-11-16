package goodsoul_andrew.spellforgeexpansion.mixin;

import goodsoul_andrew.spellforgeexpansion.SpellForgeExpansion;
import goodsoul_andrew.spellforgeexpansion.focus.FocusRequirement;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeScreen$SpellCardInfo")
public abstract class SpellCardInfoMixin {

    @Shadow AbstractSpell spell;

    private static final Field ACTIVITY_STATE_FIELD;
    private static final Object INK_ERROR;
    private boolean spellforge$isFocusError;

    static {
        try {
            Class<?> cardInfoClass = Class.forName(
                    "io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeScreen$SpellCardInfo");
            ACTIVITY_STATE_FIELD = cardInfoClass.getDeclaredField("activityState");
            ACTIVITY_STATE_FIELD.setAccessible(true);

            Class<?> stateClass = Class.forName(
                    "io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeScreen$SpellCardInfo$ActivityState");
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object inkError = Enum.valueOf((Class<Enum>) stateClass, "INK_ERROR");
            INK_ERROR = inkError;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize reflection for SpellCardInfo", e);
        }
    }

    @Inject(method = "draw", at = @At("HEAD"))
    private void spellforge$checkFocusBeforeDraw(ScrollForgeScreen screen, GuiGraphics gui, int x, int y, int mouseX, int mouseY, CallbackInfo ci) {
        this.spellforge$isFocusError = false;
        ItemStack ink = screen.getMenu().getInkSlot().getItem();
        SpellRarity inkRarity = getRarityFromInk(ink);
        if (inkRarity == null) return;

        ItemStack focus = screen.getMenu().getFocusSlot().getItem();

        if (spell.getMinRarity() > inkRarity.getValue()) {
            try {
                ACTIVITY_STATE_FIELD.set(this, INK_ERROR);
                this.spellforge$isFocusError = false;
            } catch (Exception e) {

            }
        } else if (!FocusRequirement.accepts(focus, this.spell, inkRarity)) {
            try {
                ACTIVITY_STATE_FIELD.set(this, INK_ERROR);
                spellforge$isFocusError = true;
            } catch (Exception e) {
                SpellForgeExpansion.LOGGER.error("SpellCardInfoMixin error", e);
            }
        }
    }

    @Inject(method = "getHoverText", at = @At("RETURN"), cancellable = true)
    private void spellforge$customTooltip(CallbackInfoReturnable<List<FormattedCharSequence>> cir) {
        if (!spellforge$isFocusError) return;
        List<FormattedCharSequence> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) return;
        List<FormattedCharSequence> modified = new ArrayList<>(original);
        modified.set(0, Component
                .translatable("ui.spellforge_expansion.focus_rarity_error")
                .withStyle(Style.EMPTY)
                .getVisualOrderText());

        cir.setReturnValue(modified);
    }

    private static SpellRarity getRarityFromInk(ItemStack stack) {
        if (stack.getItem() instanceof io.redspace.ironsspellbooks.item.InkItem inkItem) {
            return inkItem.getRarity();
        }
        return null;
    }
}