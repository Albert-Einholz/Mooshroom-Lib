package de.einholz.ehmooshroom.storage.storages;

import de.einholz.ehmooshroom.storage.variants.SingletonVariant;
import de.einholz.ehmooshroom.util.NbtSerializable;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

public abstract class BarStorage<T extends SingletonVariant> extends SnapshotParticipant<Long>
        implements SingleSlotStorage<T>, NbtSerializable {
    public static final long MIN = 0L;
    private long cur = MIN;
    private long last = cur;
    private long balance;
    private final BlockEntity dirtyMarker;

    public BarStorage(BlockEntity dirtyMarker) {
        this.dirtyMarker = dirtyMarker;
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        if (!supportsInsertion())
            return 0;
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long insertedAmount = Math.min(maxAmount, getCapacity());
        if (insertedAmount > 0) {
            updateSnapshots(transaction);
            setAmount(getAmount() + insertedAmount);
        }
        return insertedAmount;
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        if (!supportsExtraction())
            return 0;
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long extractedAmount = Math.min(maxAmount, getAmount());
        if (extractedAmount > 0) {
            updateSnapshots(transaction);
            setAmount(getAmount() - extractedAmount);
        }
        return extractedAmount;
    }

    @Override
    protected Long createSnapshot() {
        return getAmount();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        setAmount(snapshot);
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        dirtyMarker.markDirty();
    }

    @Override
    public boolean isResourceBlank() {
        return getAmount() <= MIN;
    }

    public boolean isFull() {
        return getCapacity() == 0;
    }

    @Override
    public long getAmount() {
        return cur;
    }

    public void setAmount(long cur) {
        this.cur = cur;
    }

    public void increase(long amount) {
        setAmount(Math.min(getMax(), getAmount() + Math.max(amount, 0)));
    }

    public void decrease(long amount) {
        setAmount(Math.max(BarStorage.MIN, getAmount() - Math.max(amount, 0)));
    }

    abstract public long getMax();

    @Override
    public long getCapacity() {
        return getMax() - getAmount();
    }

    public long getBal() {
        return balance;
    }

    public void updateBal() {
        balance = getAmount() - last;
        last = getAmount();
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putLong("Cur", getAmount());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("Cur", NbtType.NUMBER))
            setAmount(nbt.getLong("Cur"));
    }
}
