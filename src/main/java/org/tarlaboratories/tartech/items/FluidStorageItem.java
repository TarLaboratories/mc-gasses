package org.tarlaboratories.tartech.items;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.tarlaboratories.tartech.ModComponents;

import java.util.List;
import java.util.Objects;

public class FluidStorageItem extends Item {
    protected final double capacity;

    public FluidStorageItem(Settings settings) {
        super(settings);
        this.capacity = 4;
    }

    public double getFluidAmount(ItemStack stack) {
        return stack.getOrDefault(ModComponents.STORED_FLUID_AMOUNT, 0.);
    }

    public Fluid getStoredFluid(ItemStack stack) {
        String id = stack.getOrDefault(ModComponents.STORED_FLUID, "");
        if (Objects.equals(id, "") || getFluidAmount(stack) == 0) return Fluids.EMPTY;
        return Registries.FLUID.get(Identifier.of(id));
    }

    public void setStoredFluid(ItemStack stack, Fluid fluid) {
        stack.set(ModComponents.STORED_FLUID, fluid.getRegistryEntry().getIdAsString());
    }

    public void setFluidAmount(ItemStack stack, double amount) {
        stack.set(ModComponents.STORED_FLUID_AMOUNT, amount);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        BlockHitResult result = raycast(world, user, RaycastContext.FluidHandling.ANY);
        if (result.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = result.getBlockPos();
            if (getFluidAmount(stack) + 1 <= capacity) {
                FluidState fluid = world.getFluidState(pos);
                if (!fluid.isEmpty() && fluid.isStill() && (fluid.isOf(getStoredFluid(stack)) || getFluidAmount(stack) == 0)) {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    setStoredFluid(stack, fluid.getFluid());
                    setFluidAmount(stack, getFluidAmount(stack) + 1);
                    return ActionResult.SUCCESS.withNewHandStack(stack);
                }
            }
            if (getFluidAmount(stack) >= 1) {
                BlockPos pos2 = pos.offset(result.getSide());
                if (world.getBlockState(pos2).isAir()) {
                    world.setBlockState(pos2, getStoredFluid(stack).getDefaultState().getBlockState());
                    setFluidAmount(stack, getFluidAmount(stack) - 1);
                    return ActionResult.SUCCESS.withNewHandStack(stack);
                } else return ActionResult.PASS;
            } else return ActionResult.PASS;
        } else return ActionResult.PASS;
    }

    public ItemStack getWithFluid(Fluid fluid, double amount) {
        ItemStack stack = getDefaultStack();
        setStoredFluid(stack, fluid);
        setFluidAmount(stack, amount);
        return stack;
    }

    @Override
    public Text getName(ItemStack stack) {
        String tmp = getStoredFluid(stack).getBucketItem().getName().getString();
        return Text.literal("Small Fluid Tank with ").append(tmp.replaceAll("Bucket", "").replaceAll("of", "").strip());
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal(String.valueOf(getFluidAmount(stack)*1000)).append("mB/").append(String.valueOf(capacity*1000)).append("mB"));
        if (type.isAdvanced()) tooltip.add(Text.literal("Stored fluid: ").append(stack.getOrDefault(ModComponents.STORED_FLUID, "None")));
        super.appendTooltip(stack, context, tooltip, type);
    }
}
