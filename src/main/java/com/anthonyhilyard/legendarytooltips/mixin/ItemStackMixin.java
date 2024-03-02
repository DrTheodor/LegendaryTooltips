package com.anthonyhilyard.legendarytooltips.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static net.minecraft.world.item.ItemStack.TAG_DISPLAY;
import static net.minecraft.world.item.ItemStack.TAG_DISPLAY_NAME;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract @Nullable CompoundTag getTagElement(String string);

    @Shadow public abstract Item getItem();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public Component getHoverName() {
        CompoundTag compoundTag = this.getTagElement(TAG_DISPLAY);
        if (compoundTag != null && compoundTag.contains(TAG_DISPLAY_NAME, 8)) {
            try {
                MutableComponent component = Component.Serializer.fromJson(compoundTag.getString(TAG_DISPLAY_NAME));

                if (component != null)
                    return stripLeading(component, true);

                compoundTag.remove(TAG_DISPLAY_NAME);
            }
            catch (Exception exception) {
                compoundTag.remove(TAG_DISPLAY_NAME);
            }
        }

        return this.getItem().getName((ItemStack) (Object) this);
    }

    @Unique
    private static MutableComponent stripLeading(MutableComponent component, boolean first) {
        MutableComponent copy;

        if (component.getContents() instanceof LiteralContents literal) {
            String text = literal.text();

            if (first) {
                copy = MutableComponent.create(new LiteralContents(text.strip()))
                        .withStyle(component.getStyle());
            } else {
                copy = component;
            }
        } else {
            copy = MutableComponent.create(component.getContents());
        }

        first = true;
        for (Component sibling : component.getSiblings()) {
            copy.append(stripLeading((MutableComponent) sibling, first));
            first = false;
        }

        return copy;
    }
}
