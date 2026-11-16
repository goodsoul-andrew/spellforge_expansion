package goodsoul_andrew.spellforgeexpansion.focus;

import goodsoul_andrew.spellforgeexpansion.SpellForgeExpansion;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.tags.ITagManager;

public final class FocusRequirement {
    private FocusRequirement() {
    }

    public static TagKey<Item> getTag(AbstractSpell spell, SpellRarity rarity) {
        ResourceLocation schoolId = spell.getSchoolType().getId();

        ResourceLocation tagId = ResourceLocation.fromNamespaceAndPath(
                SpellForgeExpansion.MOD_ID,
                "focus/" + schoolId.getPath() + "/" + rarity.getSerializedName()
        );

        return TagKey.create(Registries.ITEM, tagId);
    }

    public static boolean accepts(ItemStack focus, AbstractSpell spell, SpellRarity rarity) {
        TagKey<Item> tag = getTag(spell, rarity);
        ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        // SpellForgeExpansion.LOGGER.info("setupResultSlot: spell={}, rarity={}, focus={}", spell, rarity, focus);
        // SpellForgeExpansion.LOGGER.info("tag={}, tagManager={}, isKnownTagName={}", tag, tagManager, tagManager != null && tagManager.isKnownTagName(tag));
        if (tagManager != null && tagManager.isKnownTagName(tag)) {
            return focus.is(tag);
        }
        return SchoolRegistry.getSchoolsFromFocus(focus).contains(spell.getSchoolType());
    }
}
