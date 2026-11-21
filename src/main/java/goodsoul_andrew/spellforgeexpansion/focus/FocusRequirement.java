package goodsoul_andrew.spellforgeexpansion.focus;

import goodsoul_andrew.spellforgeexpansion.SpellForgeExpansion;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
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

    public static TagKey<Item>[] getTags(SchoolType school, SpellRarity rarity) {
        ResourceLocation schoolId = school.getId();
        int legendaryValue = SpellRarity.LEGENDARY.getValue();
        TagKey<Item>[] result = new TagKey[legendaryValue - rarity.getValue() + 1];
        SpellRarity[] rarities = SpellRarity.values();
        for (int i = rarity.getValue(); i <= legendaryValue; i++) {
            ResourceLocation tagId = ResourceLocation.fromNamespaceAndPath(
                    SpellForgeExpansion.MOD_ID,
                    "focus/" + schoolId.getPath() + "/" + rarities[i].getSerializedName()
            );
            TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
            result[i - rarity.getValue()] = tag;
        }

        return result;
    }

    public static TagKey<Item>[] getTags(AbstractSpell spell, SpellRarity rarity) {
        return  getTags(spell.getSchoolType(), rarity);
    }

    public static boolean accepts(ItemStack focus, AbstractSpell spell, SpellRarity rarity) {
        //TagKey<Item> tag = getTag(spell, rarity);
        TagKey<Item>[] tags = getTags(spell, rarity);
        ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
        if (tagManager == null) {
            return SchoolRegistry.getSchoolsFromFocus(focus).contains(spell.getSchoolType());
        }
        // SpellForgeExpansion.LOGGER.info("setupResultSlot: spell={}, rarity={}, focus={}", spell, rarity, focus);
        // SpellForgeExpansion.LOGGER.info("tag={}, tagManager={}, isKnownTagName={}", tag, tagManager, tagManager != null && tagManager.isKnownTagName(tag));
        /*if (tagManager != null && tagManager.isKnownTagName(tag)) {
            return focus.is(tag);
        }*/
        boolean found = false;
        boolean rarityTagsExist = false;
        for (TagKey<Item> tag : tags) {
            boolean known = tagManager.isKnownTagName(tag);
            if (known) {
                rarityTagsExist = true;
                if (focus.is(tag)) {
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            return true;
        } else if (rarityTagsExist) {
            return false;
        }
        return SchoolRegistry.getSchoolsFromFocus(focus).contains(spell.getSchoolType());
    }
}
