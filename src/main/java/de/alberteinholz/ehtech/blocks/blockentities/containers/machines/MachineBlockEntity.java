package de.alberteinholz.ehtech.blocks.blockentities.containers.machines;

import java.util.Optional;

import de.alberteinholz.ehtech.TechMod;
import de.alberteinholz.ehtech.blocks.blockentities.containers.ContainerBlockEntity;
import de.alberteinholz.ehtech.blocks.components.container.ContainerInventoryComponent;
import de.alberteinholz.ehtech.blocks.components.container.InventoryWrapper;
import de.alberteinholz.ehtech.blocks.components.container.ContainerInventoryComponent.Slot;
import de.alberteinholz.ehtech.blocks.components.container.ContainerInventoryComponent.Slot.Type;
import de.alberteinholz.ehtech.blocks.components.container.machine.MachineCapacitorComponent;
import de.alberteinholz.ehtech.blocks.components.container.machine.MachineDataProviderComponent;
import de.alberteinholz.ehtech.blocks.components.container.machine.MachineDataProviderComponent.ConfigBehavior;
import de.alberteinholz.ehtech.blocks.components.container.machine.MachineDataProviderComponent.ConfigType;
import de.alberteinholz.ehtech.blocks.directionals.containers.ContainerBlock;
import de.alberteinholz.ehtech.blocks.directionals.containers.machines.MachineBlock;
import de.alberteinholz.ehtech.blocks.recipes.Input;
import de.alberteinholz.ehtech.blocks.recipes.MachineRecipe;
import de.alberteinholz.ehtech.blocks.recipes.Input.ItemIngredient;
import de.alberteinholz.ehtech.registry.BlockRegistry;
import de.alberteinholz.ehtech.util.Helper;
import io.github.cottonmc.component.UniversalComponents;
import io.github.cottonmc.component.api.ActionType;
import io.github.cottonmc.component.energy.type.EnergyTypes;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class MachineBlockEntity extends ContainerBlockEntity implements Tickable {
    public MachineCapacitorComponent capacitor = initializeCapacitorComponent();
    public int powerBilanz = 0;
    public int lastPower = capacitor.getCurrentEnergy();

    public MachineBlockEntity(BlockRegistry registryEntry) {
        super(registryEntry);
        capacitor.setDataProvider((MachineDataProviderComponent) data);
        inventory.stacks.put("power_input", new ContainerInventoryComponent.Slot(ContainerInventoryComponent.Slot.Type.OTHER));
        inventory.stacks.put("power_output", new ContainerInventoryComponent.Slot(ContainerInventoryComponent.Slot.Type.OTHER));
        inventory.stacks.put("upgrade", new ContainerInventoryComponent.Slot(ContainerInventoryComponent.Slot.Type.OTHER));
        inventory.stacks.put("network", new ContainerInventoryComponent.Slot(ContainerInventoryComponent.Slot.Type.OTHER));
    }

    @Override
    public void tick() {
        MachineDataProviderComponent data = (MachineDataProviderComponent) this.data;
        boolean isRunning = data.progress.getBarCurrent() > data.progress.getBarMinimum() && isActivated();
        powerBilanz = capacitor.getCurrentEnergy() - lastPower;
        lastPower = capacitor.getCurrentEnergy();
        transfer();
        if (!isRunning && isActivated()) {
            isRunning = checkForRecipe();
        }
        if (isRunning) {
            if (data.progress.getBarCurrent() == data.progress.getBarMinimum()) {
                start();
            }
            if (process()) {
                task();
            }
            if (data.progress.getBarCurrent() == data.progress.getBarMaximum()) {
                finish();
            }
        } else {
            idle();
        }
        correct();
        markDirty();
    }

    public void transfer() {
        for (Direction dir : Direction.values()) {
            BlockPos targetPos = pos.offset(dir);
            Block targetBlock = world.getBlockState(targetPos).getBlock();
            if (targetBlock instanceof ContainerBlock) {
                ContainerInventoryComponent inv = (ContainerInventoryComponent) ((ContainerBlock) targetBlock).getComponent(world, targetPos, UniversalComponents.INVENTORY_COMPONENT, null);
                if (((MachineDataProviderComponent) data).getConfig(ConfigType.ITEM, ConfigBehavior.SELF_INPUT, dir)) {
                    inventory.pull(inv, ActionType.PERFORM, dir);
                }
                if (((MachineDataProviderComponent) data).getConfig(ConfigType.ITEM, ConfigBehavior.SELF_OUTPUT, dir)) {
                    inventory.push(inv, ActionType.PERFORM, dir);
                }
            } else if (world.getBlockEntity(targetPos) instanceof Inventory) {
                if (((MachineDataProviderComponent) data).getConfig(ConfigType.ITEM, ConfigBehavior.SELF_INPUT, dir)) {
                    Helper.pull((MachineDataProviderComponent) data, inventory, (Inventory) world.getBlockEntity(targetPos), 1, dir);
                }
                if (((MachineDataProviderComponent) data).getConfig(ConfigType.ITEM, ConfigBehavior.SELF_OUTPUT, dir)) {
                    Helper.push((MachineDataProviderComponent) data, inventory, (Inventory) world.getBlockEntity(targetPos), 1, dir);
                }
            }
            //TODO Fluid
            if (targetBlock instanceof Block) {
                if (((MachineDataProviderComponent) data).getConfig(ConfigType.FLUID, ConfigBehavior.SELF_INPUT, dir)) {
                    //TODO Fluid
                }
                if (((MachineDataProviderComponent) data).getConfig(ConfigType.FLUID, ConfigBehavior.SELF_OUTPUT, dir)) {
                    //TODO Fluid
                }
            }
            if (targetBlock instanceof MachineBlock) {
                MachineCapacitorComponent cap = (MachineCapacitorComponent) ((MachineBlock) targetBlock).getComponent(world, targetPos, UniversalComponents.CAPACITOR_COMPONENT, null);
                if (((MachineDataProviderComponent) data).getConfig(ConfigType.POWER, ConfigBehavior.SELF_INPUT, dir)) {
                    capacitor.pull(cap, ActionType.PERFORM, dir);
                }
                if (((MachineDataProviderComponent) data).getConfig(ConfigType.POWER, ConfigBehavior.SELF_OUTPUT, dir)) {
                    capacitor.push(cap, ActionType.PERFORM, dir);
                }
            }
            //only for testing TODO: remove
            if (inventory.getStack("power_input").getItem() == Items.BEDROCK && capacitor.getCurrentEnergy() < capacitor.getMaxEnergy()) {
                capacitor.generateEnergy(world, pos, 4);
            }
        }
    }

    public boolean checkForRecipe() {
        Optional<MachineRecipe> optional = world.getRecipeManager().getFirstMatch(BlockRegistry.getEntry(BlockEntityType.getId(getType())).recipeType, new InventoryWrapper(pos), world);
        ((MachineDataProviderComponent) this.data).setRecipe(optional.orElse(null));
        return optional.isPresent();
    }

    public void start() {
        MachineDataProviderComponent data = (MachineDataProviderComponent) this.data;
        MachineRecipe recipe = (MachineRecipe) data.getRecipe(world);
        boolean consumerRecipe = (recipe.consumes == Double.NaN ? 0.0 : recipe.consumes) > (recipe.generates == Double.NaN ? 0.0 : recipe.generates);
        int consum = (int) (data.getEfficiency() * data.getSpeed() * recipe.consumes);
        if ((consumerRecipe && capacitor.extractEnergy(capacitor.getPreferredType(), consum, ActionType.TEST) == consum) || !consumerRecipe) {
            for (ItemIngredient ingredient : recipe.input.items) {
                int consumingLeft = ingredient.amount;
                for (Slot slot : inventory.stacks.values()) {
                    if (slot.type == Type.INPUT && ingredient.ingredient.contains(slot.stack.getItem()) && NbtHelper.matches(ingredient.tag, slot.stack.getTag(), true)) {
                        if (slot.stack.getCount() >= consumingLeft) {
                            slot.stack.decrement(consumingLeft);
                            break;
                        } else {
                            consumingLeft -= slot.stack.getCount();
                            slot.stack.setCount(0);
                        }
                    }
                }
            }
        }
    }

    public boolean process() {
        MachineDataProviderComponent data = (MachineDataProviderComponent) this.data;
        MachineRecipe recipe = (MachineRecipe) data.getRecipe(world);
        boolean doConsum = recipe.consumes != Double.NaN && recipe.consumes > 0.0;
        boolean canConsum = true;
        int consum = 0;
        boolean doGenerate = recipe.generates != Double.NaN && recipe.generates > 0.0;
        boolean canGenerate = true;
        int generation = 0;
        boolean canProcess = true;
        if (doConsum) {
            consum = (int) (data.getEfficiency() * data.getSpeed() * recipe.consumes);
            if (!(capacitor.extractEnergy(capacitor.getPreferredType(), consum, ActionType.TEST) == consum)) {
                canConsum = false;
            }
        }
        if (doGenerate) {
            generation = (int) (data.getEfficiency() * data.getSpeed() * recipe.generates);
            if (!(capacitor.getCurrentEnergy() + generation <= capacitor.getMaxEnergy())) {
                canGenerate = false;
            }
        }
        if (doConsum) {
            if (canConsum && canGenerate) {
                capacitor.extractEnergy(capacitor.getPreferredType(), consum, ActionType.PERFORM);
            } else {
                canProcess = false;
            }
        }
        if (doGenerate) {
            if (canConsum && canGenerate) {
                capacitor.generateEnergy(world, pos, generation);
            } else {
                canProcess = false;
            }
        }
        if (canProcess) {
            data.addProgress(recipe.timeModifier * data.getSpeed());
        }
        return canProcess;
    }

    public void task() {

    }

    public void finish() {
        cancle();
    }

    public void cancle() {
        MachineDataProviderComponent data = (MachineDataProviderComponent) this.data;
        data.resetProgress();
        data.resetRecipe();
    }

    public void idle() {

    }

    public void correct() {

    }

    public boolean containsItemIngredients(Input.ItemIngredient... ingredients) {
        boolean bl = true;
        for (Input.ItemIngredient ingredient : ingredients) {
            if (!inventory.containsInput(ingredient)) {
                bl = false;
            }
        }
        return bl;
    }

    public boolean containsFluidIngredients(Input.FluidIngredient... ingredients) {
        boolean bl = true;
        for (Input.FluidIngredient ingredient : ingredients) {
            TechMod.LOGGER.wip("Containment Check for " + ingredient);
            //TODO Fluid
        }
        return bl;
    }

    //only by overriding
    public boolean containsBlockIngredients(Input.BlockIngredient... ingredients) {
        return true;
    }

    //only by overriding
    public boolean containsEntityIngredients(Input.EntityIngredient... ingredients) {
        return true;
    }

    //only by overriding
    public boolean containsDataIngredients(Input.DataIngredient... ingredients) {
        return true;
    }

    public boolean isActivated() {
        MachineDataProviderComponent.ActivationState activationState = ((MachineDataProviderComponent) data).getActivationState();
        if (activationState == MachineDataProviderComponent.ActivationState.ALWAYS_ON) {
            return true;
        } else if(activationState == MachineDataProviderComponent.ActivationState.REDSTONE_ON) {
            return world.isReceivingRedstonePower(pos);
        } else if(activationState == MachineDataProviderComponent.ActivationState.REDSTONE_OFF) {
            return !world.isReceivingRedstonePower(pos);
        } else {
            return false;
        }
    }

    @Override
    public void fromTag(BlockState state,CompoundTag tag) {
        super.fromTag(state, tag);
        if (world != null && tag.contains("Capacitor", NbtType.COMPOUND)) {
            capacitor.fromTag(tag.getCompound("Capacitor"));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        if (world != null) {
            CompoundTag capacitorTag = new CompoundTag();
            capacitor.toTag(capacitorTag);
            if (!capacitorTag.isEmpty()) {
                tag.put("Capacitor", capacitorTag);
            }
        }
        return tag;
    }
    
    protected MachineCapacitorComponent initializeCapacitorComponent() {
        return new MachineCapacitorComponent(EnergyTypes.ULTRA_LOW_VOLTAGE);
    }

    @Override
    protected MachineDataProviderComponent initializeDataProviderComponent() {
        return (MachineDataProviderComponent) data;
    }
}