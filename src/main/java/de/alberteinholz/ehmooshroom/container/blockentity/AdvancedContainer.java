package de.alberteinholz.ehmooshroom.container.blockentity;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import de.alberteinholz.ehmooshroom.MooshroomLib;
import de.alberteinholz.ehmooshroom.container.component.ConfigDataComponent;
import de.alberteinholz.ehmooshroom.container.component.ContainerInventoryComponent;
import de.alberteinholz.ehmooshroom.container.component.NameDataComponent;
import de.alberteinholz.ehmooshroom.registry.RegistryEntry;
import de.alberteinholz.ehmooshroom.util.Helper;
import io.netty.buffer.Unpooled;
import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public abstract class AdvancedContainer extends BlockEntity implements BlockEntityClientSerializable, ExtendedScreenHandlerFactory {
    protected final RegistryEntry registryEntry;
    public Map<Identifier, Component> comps = new HashMap<>();

    public AdvancedContainer(RegistryEntry registryEntry) {
        super(registryEntry.blockEntityType);
        this.registryEntry = registryEntry;
        comps.put(Helper.makeId("name"), new NameDataComponent("name"));
        comps.put(Helper.makeId("config"), new ConfigDataComponent());
        comps.forEach((id, comp) -> {
            if (comp instanceof ContainerInventoryComponent) ((ContainerInventoryComponent) comp).setConfig((ConfigDataComponent) comps.get(Helper.makeId("config")));
        });
    }

    //you have to add all needed components first
    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        if (world == null) return;
        for (String key : tag.getKeys()) {
            Identifier id = new Identifier(key);
            if (!tag.contains(key, NbtType.COMPOUND) || tag.getCompound(key).isEmpty()) continue;
            if (!comps.containsKey(id)) {
                MooshroomLib.LOGGER.smallBug(new NoSuchElementException("There is no component with the id " + key + " in the AdvancedContainer" + getDisplayName().getString()));
                continue;
            }
            CompoundTag compTag = tag.getCompound(key);
            comps.get(id).fromTag(compTag);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        if (world == null) return tag;
        comps.forEach((id, comp) -> {
            CompoundTag compTag = new CompoundTag();
            comp.toTag(compTag);
            if (!compTag.isEmpty()) tag.put(id.toString(), compTag);
        });
        return tag;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void fromClientTag(CompoundTag tag) {
        fromTag(world.getBlockState(pos), tag);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return toTag(tag);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(((NameDataComponent) comps.get(Helper.makeId("name"))).containerName.getLabel().asString());
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInv, PlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        writeScreenOpeningData((ServerPlayerEntity) player, buf);
        return registryEntry.clientHandlerFactory.create(syncId, playerInv, buf);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }
}