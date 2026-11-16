# Spell Forge Expansion

Forge 1.20.1 addon for Iron's Spells 'n Spellbooks 3.16.3.

The addon adds rarity-specific focus tags:
`spellforge_expansion:focus/<school>/<rarity>`

Example:
`spellforge_expansion:focus/evocation/epic`

Important: you must add new items from rarity focus tags to default school tag!  
Example:  
`spellforge_expansion/tags/items/focus/evocation/epic.json`
```json
{
  "values": [
    "minecraft:emerald_block"
  ]
}
```
`irons_spellbooks/tags/items/evocation_focus.json`
```json
{
  "values": [
    "minecraft:emerald",
    "minecraft:emerald_block"
  ]
}
```

If rarity focus tag for school exists, these spells can be crafted only with items that match the tag, otherwise crafting is unchanged.

By default, epic and legendary evocation spells require emerald block.

The JSON tags are deliberately data-driven, so pack makers can change the
required items without changing Java code.
