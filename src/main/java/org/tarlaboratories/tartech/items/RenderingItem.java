package org.tarlaboratories.tartech.items;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class RenderingItem extends Item {
    public RenderingItem(Settings settings) {
        super(settings);
    }

    public ItemStack getWithModel(String model) {
        ItemStack out = this.getDefaultStack();
        out.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(List.of(), List.of(), List.of(model), List.of()));
        return out;
    }
}
