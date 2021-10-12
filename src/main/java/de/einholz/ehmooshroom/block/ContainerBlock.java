package de.einholz.ehmooshroom.block;

import de.einholz.ehmooshroom.container.AdvancedContainerBE;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public abstract class ContainerBlock extends DirectionalBlock implements InventoryProvider {
    public Identifier id;

    public ContainerBlock(FabricBlockSettings settings, Identifier id) {
        super(settings);
        this.id = id;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) player.openHandledScreen((AdvancedContainerBE) world.getBlockEntity(pos));
        return ActionResult.SUCCESS;
    }

    //FIXME: two times super method???
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.isClient) super.onBreak(world, pos, state, player);
        ItemStack itemStack = new ItemStack(asItem());
        NbtCompound nbtCompound = world.getBlockEntity(pos).writeNbt(new NbtCompound());
        nbtCompound.remove("x");
        nbtCompound.remove("y");
        nbtCompound.remove("z");
        nbtCompound.remove("id");
        if (!nbtCompound.isEmpty()) itemStack.putSubTag("BlockEntityTag", nbtCompound);
        ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), itemStack);
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
        super.onBreak(world, pos, state, player);
    }

    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        return null;//TODO
    }
}