package de.einholz.ehmooshroom.registry;

import java.util.function.BiFunction;
import java.util.function.Function;

import de.einholz.ehmooshroom.MooshroomLib;
import de.einholz.ehmooshroom.registry.helpers.BlockEntitiesREB;
import de.einholz.ehmooshroom.registry.helpers.BlocksREB;
import de.einholz.ehmooshroom.registry.helpers.GuisREB;
import de.einholz.ehmooshroom.registry.helpers.ItemsREB;
import de.einholz.ehmooshroom.registry.helpers.RecipesREB;
import de.einholz.ehmooshroom.util.LoggerHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class RegEntryBuilder<B extends BlockEntity, G extends ScreenHandler, S extends HandledScreen<G>, R extends Recipe<?>> implements BlocksREB<B, G, S, R>, BlockEntitiesREB<B, G, S, R>, ItemsREB<B, G, S, R>, GuisREB<B, G, S, R>, RecipesREB<B, G, S, R> {
    private Identifier id;
    private Function<RegEntryBuilder<B, G, S, R>, Block> blockFunc = (entry) -> null;
    private Block block;
    private Function<RegEntryBuilder<B, G, S, R>, BlockApiProvider<Storage<ItemVariant>, Direction>> blockItemStorageProvFunc = (entry) -> null;
    private Function<RegEntryBuilder<B, G, S, R>, BlockApiProvider<Storage<FluidVariant>, Direction>> blockFluidStorageProvFunc = (entry) -> null;
    private Function<RegEntryBuilder<B, G, S, R>, BlockEntityType<B>> blockEntityTypeFunc = (entry) -> null;
    private BlockEntityType<B> blockEntityType;
    private Function<RegEntryBuilder<B, G, S, R>, BiFunction<B, Direction, Storage<ItemVariant>>> blockEntityItemStorageProvFunc = (entry) -> null;
    private Function<RegEntryBuilder<B, G, S, R>, BiFunction<B, Direction, Storage<FluidVariant>>> blockEntityFluidStorageProvFunc = (entry) -> null;
    private Function<RegEntryBuilder<B, G, S, R>, Item> itemFunc = (entry) -> null;
    private Item item;
    private Function<RegEntryBuilder<B, G, S, R>, Integer> fuelTicks;
    private Function<RegEntryBuilder<B, G, S, R>, ScreenHandlerType<G>> guiFunc = (entry) -> null;
    private ScreenHandlerType<G> gui;
    private Function<RegEntryBuilder<B, G, S, R>, ScreenRegistry.Factory<G, S>> screenFunc = (entry) -> null;
    private ScreenRegistry.Factory<G, S> screen;
    private Function<RegEntryBuilder<B, G, S, R>, RecipeType<R>> recipeTypeFunc = (entry) -> null;
    private RecipeType<R> recipeType;
    private Function<RegEntryBuilder<B, G, S, R>, RecipeSerializer<R>> recipeSerializerFunc = (entry) -> null;
    private RecipeSerializer<R> recipeSerializer;

    /*
    private Factory<B> blockEntityTypeFactory;

    //supplied:
    private ItemGroup itemGroup;
    //private ExtendedClientHandlerFactory<G> clientHandlerFactory;
    //private Factory<ScreenHandler, HandledScreen<ScreenHandler>> screenFactory;
    private RecipeType<? extends Recipe<?>> recipeType;
    private RecipeSerializer<? extends Recipe<?>> recipeSerializer;
    //created:
    //private ScreenHandlerType<G> screenHandlerType;

    @Deprecated // TODO del if unused
    @SuppressWarnings("unchecked") // XXX is there a better way to do this?
    public RegEntryBuilder<B, G, S, R> applyTemplate(Function<RegEntryBuilder<B, G, S, R>, ? extends RegEntryBuilder<? extends B, ? extends G, ? extends S>> template) {
        return (RegEntryBuilder<B, G, S, R>) template.apply(this);
    }
    */

    protected LoggerHelper getLogger() {
        return MooshroomLib.LOGGER;
    }

    protected Function<String, Identifier> getEasyIdFactory() {
        return MooshroomLib.HELPER::makeId;
    }

    public Identifier getId() {
        if (id == null) {
            getLogger().smallBug(new NullPointerException("Identifier is null! Probably it wasn't assigned yet"));
            return getEasyIdFactory().apply("invalid");
        }
        return id;
    }

    // BLOCKS
    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public RegEntryBuilder<B, G, S, R> withBlockRaw(Function<RegEntryBuilder<B, G, S, R>, Block> blockFunc) {
        this.blockFunc = blockFunc;
        return this;
    }

    @Override
    public RegEntryBuilder<B, G, S, R> withBlockItemStorageProvRaw(Function<RegEntryBuilder<B, G, S, R>, BlockApiProvider<Storage<ItemVariant>, Direction>> blockItemStorageProvFunc) {
        this.blockItemStorageProvFunc = blockItemStorageProvFunc;
        return this;
    }

    @Override
    public RegEntryBuilder<B, G, S, R> withBlockFluidStorageProvRaw(Function<RegEntryBuilder<B, G, S, R>, BlockApiProvider<Storage<FluidVariant>, Direction>> blockFluidStorageProvFunc) {
        this.blockFluidStorageProvFunc = blockFluidStorageProvFunc;
        return this;
    }

    // BLOCK ENTITIES
    public BlockEntityType<B> getBlockEntityType() {
        return blockEntityType;
    }

    @Override
    public RegEntryBuilder<B, G, S, R> withBlockEntityRaw(Function<RegEntryBuilder<B, G, S, R>, BlockEntityType<B>> blockEntityTypeFunc) {
        this.blockEntityTypeFunc = blockEntityTypeFunc;
        return this;
    }

    @Override
    public RegEntryBuilder<B, G, S, R> withBlockEntityItemStorageProvRaw(Function<RegEntryBuilder<B, G, S, R>, BiFunction<B, Direction, Storage<ItemVariant>>> blockEntityItemStorageProvFunc) {
        this.blockEntityItemStorageProvFunc = blockEntityItemStorageProvFunc;
        return this;
    }

    @Override
    public RegEntryBuilder<B, G, S, R> withBlockEntityFluidStorageProvRaw(Function<RegEntryBuilder<B, G, S, R>, BiFunction<B, Direction, Storage<FluidVariant>>> blockEntityFluidStorageProvFunc) {
        this.blockEntityFluidStorageProvFunc = blockEntityFluidStorageProvFunc;
        return this;
    }

    // ITEMS
    public Item getItem() {
        return item;
    }

    @Override
    public RegEntryBuilder<B, G, S, R> withItemRaw(Function<RegEntryBuilder<B, G, S, R>, Item> itemFunc) {
        this.itemFunc = itemFunc;
        return this;
    }

    @Override
    public RegEntryBuilder<B, G, S, R> withFuelRaw(Function<RegEntryBuilder<B, G, S, R>, Integer> fuelTicks) {
        this.fuelTicks = fuelTicks;
        return this;
    }

    // GUIS
    public ScreenHandlerType<G> getGui() {
        return gui;
    }

    @Override
    public RegEntryBuilder<B, G, S, R> withGuiRaw(Function<RegEntryBuilder<B, G, S, R>, ScreenHandlerType<G>> guiFunc) {
        this.guiFunc = guiFunc;
        return this;
    }

    public ScreenRegistry.Factory<G, S> getScreen() {
        return screen;
    }

    @Override
    public RegEntryBuilder<B, G, S, R> withScreenRaw(Function<RegEntryBuilder<B, G, S, R>, ScreenRegistry.Factory<G, S>> screenFunc) {
        this.screenFunc = screenFunc;
        return this;
    }

    // RECIPES
    public RecipeType<R> getRecipeType() {
        return recipeType;
    }

    @Override
    public RegEntryBuilder<B, G, S, R> withRecipeTypeRaw(Function<RegEntryBuilder<B, G, S, R>, RecipeType<R>> recipeTypeFunc) {
        this.recipeTypeFunc = recipeTypeFunc;
        return this;
    }

    public RecipeSerializer<R> getRecipeSerializer() {
        return recipeSerializer;
    }

    @Override
    public RegEntryBuilder<B, G, S, R> withRecipeSerializerRaw(Function<RegEntryBuilder<B, G, S, R>, RecipeSerializer<R>> recipeSerializerFunc) {
        this.recipeSerializerFunc = recipeSerializerFunc;
        return this;
    }

    // BUILDING
    public RegEntry<B, G, S, R> build(String path) {
        return build(getEasyIdFactory().apply(path));
    }

    public RegEntry<B, G, S, R> build(Identifier id) {
        this.id = id;
        block = blockFunc.apply(this);
        if (getBlock() != null) {
            // XXX faster if registerForBlockEntities is used?
            if (blockItemStorageProvFunc.apply(this) != null)
                ItemStorage.SIDED.registerForBlocks(blockItemStorageProvFunc.apply(this), getBlock());
            if (blockFluidStorageProvFunc.apply(this) != null)
                FluidStorage.SIDED.registerForBlocks(blockFluidStorageProvFunc.apply(this), getBlock());
        }
        blockEntityType = blockEntityTypeFunc.apply(this);
        if (getBlockEntityType() != null) {
            // XXX faster if registerForBlockEntities is used?
            if (blockEntityItemStorageProvFunc.apply(this) != null)
                ItemStorage.SIDED.registerForBlockEntity(blockEntityItemStorageProvFunc.apply(this), getBlockEntityType());
            if (blockEntityFluidStorageProvFunc.apply(this) != null)
                FluidStorage.SIDED.registerForBlockEntity(blockEntityFluidStorageProvFunc.apply(this), getBlockEntityType());
        }
        item = itemFunc.apply(this);
        if (fuelTicks != null) {
            if (getItem() == null) getLogger().smallBug(new NullPointerException("You must add an Item before making it a fuel for " + id.toString()));
            else FuelRegistry.INSTANCE.add(getItem(), fuelTicks.apply(this));
        }
        gui = guiFunc.apply(this);
        screen = screenFunc.apply(this);
        if (EnvType.CLIENT.equals(FabricLoader.getInstance().getEnvironmentType()) && getGui() != null && getScreen() != null) {
            ScreenRegistry.register(getGui(), getScreen());
            //HandledScreens.register
            //HandledScreens.<SyncedGuiDescription, CottonInventoryScreen<? extends SyncedGuiDescription>>register(GUI, (gui, inventory, title) -> new ContainerScreen(gui, inventory.player, title));
        }
        recipeType = recipeTypeFunc.apply(this);
        recipeSerializer = recipeSerializerFunc.apply(this);
        return new RegEntry<B, G, S, R>(getId(), getBlock(), getBlockEntityType(), getItem(), getGui(), getScreen(), getRecipeType(), getRecipeSerializer());
    }
}
