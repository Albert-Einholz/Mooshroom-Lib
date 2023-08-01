package de.einholz.ehmooshroom.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import de.einholz.ehmooshroom.storage.SideConfigType.SideConfigAccessor;
import de.einholz.ehmooshroom.storage.storages.AdvCombinedStorage;
import de.einholz.ehmooshroom.util.NbtSerializable;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class SidedStorageMgr implements NbtSerializable {
    private final Map<Identifier, StorageEntry<?, ? extends TransferVariant<?>>> STORAGES = new HashMap<>();
    private final BlockEntity dirtyMarker;

    public SidedStorageMgr(BlockEntity dirtyMarker) {
        this.dirtyMarker = dirtyMarker;
    }

    public List<Identifier> getIds() {
        return new ArrayList<>(STORAGES.keySet());
    }

    public List<Identifier> getAvaialableIds() {
        List<Identifier> list = new ArrayList<>();
        STORAGES.forEach((id, entry) -> {
            if (entry.available())
                list.add(id);
        });
        return list;
    }

    public <T, V extends TransferVariant<T>> SidedStorageMgr withStorage(Identifier id, Transferable<T, V> trans,
            Storage<V> storage) {
        STORAGES.put(id, new StorageEntry<T, V>(storage, SideConfigType.getDefaultArray(), trans, dirtyMarker));
        return this;
    }

    public SidedStorageMgr removeStorage(Identifier id) {
        STORAGES.remove(id);
        return this;
    }

    public StorageEntry<?, ? extends TransferVariant<?>> getEntry(Identifier id) {
        return STORAGES.get(id);
    }

    public <T, V extends TransferVariant<T>, S extends Storage<V>> AdvCombinedStorage<T, V, S> getCombinedStorage(
            @Nullable Transferable<T, V> trans, SideConfigAccessor acc, @Nullable SideConfigType... configTypes) {
        return new AdvCombinedStorage<T, V, S>(acc, getStorageEntries(trans, configTypes));
    }

    // XXX private? to hacky?
    /*
     * If trans or configTypes are null they will accept all
     * Transferables/SideConfigTypes
     */
    @SuppressWarnings("unchecked")
    public <T, V extends TransferVariant<T>> List<StorageEntry<T, V>> getStorageEntries(
            @Nullable Transferable<T, V> trans, @Nullable SideConfigType... configTypes) {
        List<StorageEntry<T, V>> list = new ArrayList<>();
        for (var storageEntry : STORAGES.values()) {
            if (trans != null && !trans.equals(storageEntry.trans))
                continue;
            if (configTypes == null) {
                list.add((StorageEntry<T, V>) storageEntry);
                continue;
            }
            for (SideConfigType configType : configTypes) {
                if (!storageEntry.allows(configType))
                    continue;
                list.add((StorageEntry<T, V>) storageEntry);
                break;
            }
        }
        return list;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        NbtCompound sidedStorageMgrNbt = new NbtCompound();
        for (Entry<Identifier, StorageEntry<?, ? extends TransferVariant<?>>> entry : STORAGES.entrySet()) {
            NbtCompound entryNbt = new NbtCompound();
            entry.getValue().writeNbt(entryNbt);
            if (entryNbt.isEmpty())
                continue;
            sidedStorageMgrNbt.put(entry.getKey().toString(), entryNbt);
        }
        if (!sidedStorageMgrNbt.isEmpty())
            nbt.put("SidedStorageMgr", sidedStorageMgrNbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (!nbt.contains("SidedStorageMgr", NbtType.COMPOUND))
            return;
        NbtCompound sidedStorageMgrNbt = nbt.getCompound("SidedStorageMgr");
        for (Entry<Identifier, StorageEntry<?, ? extends TransferVariant<?>>> entry : STORAGES.entrySet()) {
            if (!sidedStorageMgrNbt.contains(entry.getKey().toString(), NbtType.COMPOUND))
                continue;
            entry.getValue().readNbt(sidedStorageMgrNbt.getCompound(entry.getKey().toString()));
        }
    }
}
