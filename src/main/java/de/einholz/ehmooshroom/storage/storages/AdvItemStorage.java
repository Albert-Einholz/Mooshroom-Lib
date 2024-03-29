package de.einholz.ehmooshroom.storage.storages;

import java.util.Iterator;
import java.util.List;

import org.spongepowered.include.com.google.gson.JsonIOException;

import de.einholz.ehmooshroom.MooshroomLib;
import de.einholz.ehmooshroom.storage.AdvInv;
import de.einholz.ehmooshroom.util.NbtSerializable;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

public class AdvItemStorage extends SnapshotParticipant<ItemStack[]> implements InventoryStorage, NbtSerializable {
    private InventoryStorage storage;
    private final Inventory inv;
    private final BlockEntity dirtyMarker;

    public AdvItemStorage(final BlockEntity dirtyMarker, final int size) {
        this(dirtyMarker, new AdvInv(size));
    }

    public AdvItemStorage(final BlockEntity dirtyMarker, final Identifier... ids) {
        this(dirtyMarker, new AdvInv(ids));
    }

    private AdvItemStorage(final BlockEntity dirtyMarker, final Inventory inventory) {
        storage = InventoryStorage.of(inventory, null);
        this.inv = inventory;
        this.dirtyMarker = dirtyMarker;
    }

    @Override
    protected ItemStack[] createSnapshot() {
        ItemStack[] stacks = new ItemStack[getInv().size()];
        for (int i = 0; i < stacks.length; i++)
            stacks[i] = getInv().getStack(i);
        return stacks;
    }

    @Override
    protected void readSnapshot(ItemStack[] snapshot) {
        getInv().clear();
        for (int i = 0; i < snapshot.length; i++)
            getInv().setStack(i, snapshot[i]);
        storage = InventoryStorage.of(inv, null);
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        dirtyMarker.markDirty();
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getSlots() {
        return storage.getSlots();
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (!supportsInsertion())
            return 0;
        return storage.insert(resource, maxAmount, transaction);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (!supportsExtraction())
            return 0;
        return storage.extract(resource, maxAmount, transaction);
    }

    @Override
    public Iterator<? extends StorageView<ItemVariant>> iterator(TransactionContext transaction) {
        return storage.iterator(transaction);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (int i = 0; i < getInv().size(); i++) {
            ItemStack stack = getInv().getStack(i);
            if (stack.isEmpty())
                continue;
            NbtCompound slotNbt = new NbtCompound();
            slotNbt.putInt("Index", i);
            slotNbt.put("Stack", stack.writeNbt(new NbtCompound()));
            list.add(slotNbt);
        }
        if (!list.isEmpty())
            nbt.put("Inv", list);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        getInv().clear();
        NbtList list = nbt.getList("Inv", NbtType.COMPOUND);
        if (!list.isEmpty())
            for (NbtElement slotNbtElement : list) {
                if (slotNbtElement instanceof NbtCompound slotNbt) {
                    var stack = ItemStack.fromNbt(slotNbt.getCompound("Stack"));
                    if (stack.isEmpty())
                        continue;
                    getInv().setStack(slotNbt.getInt("Index"), stack);
                } else
                    MooshroomLib.LOGGER
                            .warnRaw(new JsonIOException("NbtElement for AdvItemStorage has to be a NbtCompound"));
            }
        storage = InventoryStorage.of(inv, null);
    }

    public Inventory getInv() {
        return inv;
    }
}
