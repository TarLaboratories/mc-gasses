package org.tarlaboratories.tartech.client.renderers.entity;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import org.tarlaboratories.tartech.Tartech;

public class ModelLayers {
    public static final EntityModelLayer MODEL_DATA_LAYER = new EntityModelLayer(Identifier.of(Tartech.MOD_ID, "data_entity"), "main");
}
