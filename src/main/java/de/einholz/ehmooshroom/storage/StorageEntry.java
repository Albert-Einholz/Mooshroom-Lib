package de.einholz.ehmooshroom.storage;

import de.einholz.ehmooshroom.MooshroomLib;
import de.einholz.ehmooshroom.util.NbtSerializable;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

public class StorageEntry<T, U extends TransferVariant<T>> implements NbtSerializable {
    public final Storage<U> storage;
    private final char[] config;
    public final Transferable<T, U> trans;
    private final BlockEntity dirtyMarker;

    public StorageEntry(Storage<U> storage, char[] config, Transferable<T, U> trans, BlockEntity dirtyMarker) {
        this.storage = storage;
        SideConfigType[] values = SideConfigType.values();
        if (config.length != values.length) {
            MooshroomLib.LOGGER
                    .warnBug("The config char array should have a lenght of " + values.length);
            char[] newConfig = new char[values.length];
            for (int i = 0; i < config.length; i++)
                newConfig[i] = config[i];
            for (int i = config.length; i < values.length; i++)
                newConfig[i] = values[i].getDefaultChar();
            this.config = newConfig;
        } else
            this.config = config;
        this.trans = trans;
        this.dirtyMarker = dirtyMarker;
    }

    public void change(SideConfigType type) {
        for (int i = 0; i < SideConfigType.CHARS.length; i++) {
            if (SideConfigType.CHARS[i] != config[type.ordinal()])
                continue;
            config[type.ordinal()] = SideConfigType.CHARS[i ^ 0x0001];
            dirtyMarker.markDirty();
            return;
        }
    }

    public boolean available(SideConfigType type) {
        return Character.isUpperCase(config[type.ordinal()]);
    }

    public boolean available() {
        for (char c : config)
            if (!Character.isLowerCase(c))
                return true;
        return false;
    }

    /**
     * @param available
     * @param types     if types == null this will default to
     *                  SideConfigType.values()
     */
    public void setAvailability(boolean available, SideConfigType... types) {
        if (types == null)
            types = SideConfigType.values();
        for (SideConfigType type : types)
            if (Character.toUpperCase(config[type.ordinal()]) == 'T')
                config[type.ordinal()] = available ? 'T' : 't';
            else
                config[type.ordinal()] = available ? 'F' : 'f';
    }

    public boolean allows(SideConfigType type) {
        if (Character.toUpperCase(config[type.ordinal()]) != 'T')
            return false;
        return type.OUTPUT ? storage.supportsExtraction() : storage.supportsInsertion();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        if (storage instanceof NbtSerializable seri) {
            NbtCompound storageNbt = seri.writeNbt(new NbtCompound());
            if (!storageNbt.isEmpty())
                nbt.put("Storage", storageNbt);
        }
        nbt.putString("Config", String.valueOf(config));
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (storage instanceof NbtSerializable seri && nbt.contains("Storage", NbtType.COMPOUND))
            seri.readNbt(nbt.getCompound("Storage"));
        if (nbt.contains("Config", NbtType.STRING)) {
            String str = nbt.getString("Config");
            if (str.length() < config.length) {
                MooshroomLib.LOGGER.warnBug("Config string for " + trans.getId() + " has a lenght of " + str.length()
                        + " but should have " + config.length);
                return;
            }
            for (int i = 0; i < config.length; i++)
                config[i] = str.charAt(i);
        }
    }
}
