package de.einholz.ehmooshroom.registry;

import de.einholz.ehmooshroom.MooshroomLib;
import de.einholz.ehmooshroom.storage.transferable.BlockVariant;
import de.einholz.ehmooshroom.storage.transferable.ElectricityVariant;
import de.einholz.ehmooshroom.storage.transferable.EntityVariant;
import de.einholz.ehmooshroom.storage.transferable.HeatVariant;
import de.einholz.ehmooshroom.storage.transferable.Transferable;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public final class TransferablesReg {
    public static final Identifier TRANSFERABLE_ID = MooshroomLib.HELPER.makeId("transferables");
    @SuppressWarnings("rawtypes") // FIXME is there a way to do this without suppressings warnings
    public static final Registry<Transferable> TRANSFERABLE = FabricRegistryBuilder.createSimple(Transferable.class, TRANSFERABLE_ID).attribute(RegistryAttribute.SYNCED).buildAndRegister();
    public static final RegistryKey<Registry<Transferable<?>>> TRANSFERABLE_KEY = RegistryKey.ofRegistry(TRANSFERABLE_ID);

    public static final Transferable<ItemVariant> ITEMS = registerMooshroom("items", new Transferable<ItemVariant>(ItemVariant.class, ItemStorage.SIDED));
    public static final Transferable<FluidVariant> FLUIDS = registerMooshroom("fluids", new Transferable<FluidVariant>(FluidVariant.class, FluidStorage.SIDED));
    public static final Transferable<BlockVariant> BLOCKS = registerMooshroom("blocks", new Transferable<BlockVariant>(BlockVariant.class, null));
    public static final Transferable<EntityVariant> ENTITIES = registerMooshroom("entities", new Transferable<EntityVariant>(EntityVariant.class, null));
    public static final Transferable<ElectricityVariant> ELECTRICITY = registerMooshroom("electricity", new Transferable<ElectricityVariant>(ElectricityVariant.class, null));
    public static final Transferable<HeatVariant> HEAT = registerMooshroom("heat", new Transferable<HeatVariant>(HeatVariant.class, null));

    private static <T> Transferable<T> registerMooshroom(String str, Transferable<T> trans) {
        return register(MooshroomLib.HELPER.makeId(str), trans);
    }

    public static <T> Transferable<T> register(Identifier id, Transferable<T> trans) {
        trans = Registry.register(TRANSFERABLE, id, trans);
        trans.setId(id);
        return trans;
    }
}
