package org.tarlaboratories.tartech.client.renderers.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;
import org.tarlaboratories.tartech.Tartech;
import org.tarlaboratories.tartech.client.models.entity.DataEntityModel;
import org.tarlaboratories.tartech.entities.DataEntity;

public class DataEntityRenderer extends MobEntityRenderer<DataEntity, LivingEntityRenderState, DataEntityModel> {
    public DataEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new DataEntityModel(context.getPart(ModelLayers.MODEL_DATA_LAYER)), 0);
    }

    @Override
    public Identifier getTexture(LivingEntityRenderState state) {
        return Identifier.of(Tartech.MOD_ID, "textures/entity/nothing.png");
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
