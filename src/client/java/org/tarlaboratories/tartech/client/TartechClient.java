package org.tarlaboratories.tartech.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import org.tarlaboratories.tartech.ModEntities;
import org.tarlaboratories.tartech.client.models.entity.DataEntityModel;
import org.tarlaboratories.tartech.client.renderers.entity.DataEntityRenderer;
import org.tarlaboratories.tartech.client.renderers.entity.ModelLayers;

public class TartechClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.DATA_ENTITY, DataEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(ModelLayers.MODEL_DATA_LAYER, DataEntityModel::getTexturedModelData);
    }
}
