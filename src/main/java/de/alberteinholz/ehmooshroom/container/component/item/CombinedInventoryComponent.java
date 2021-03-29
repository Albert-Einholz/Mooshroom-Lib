package de.alberteinholz.ehmooshroom.container.component.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.github.cottonmc.component.api.ActionType;
import io.github.cottonmc.component.item.InventoryComponent;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class CombinedInventoryComponent implements InventoryComponent {
    protected final List<Runnable> listeners = new ArrayList<>();
    protected final InventoryComponent[] childComps;
    protected int tempSlot = 0;

    public CombinedInventoryComponent(InventoryComponent... comps) {
        childComps = comps;
    }

    @Override
    public List<Runnable> getListeners() {
        return listeners;
    }

    @Override
    public boolean canExtract(int slot) {
        InventoryComponent comp = getCompFromSlot(slot);
        return (comp.equals(null)) ? false : comp.canExtract(tempSlot);
    }

    @Override
    public boolean canInsert(int slot) {
        InventoryComponent comp = getCompFromSlot(slot);
        return (comp.equals(null)) ? false : comp.canInsert(tempSlot);
    }

    @Override
    public DefaultedList<ItemStack> getMutableStacks() {
		DefaultedList<ItemStack> ret = DefaultedList.ofSize(size(), ItemStack.EMPTY);
        int i = 0;
        for (InventoryComponent comp : childComps) for (ItemStack stack : comp.getMutableStacks()) {
            ret.set(i, stack);
            i++;
        }
        return ret;
    }

    @Override
    public int size() {
        int slots = 0;
        for (InventoryComponent comp : childComps) slots += comp.size();
        return slots;
    }

    @Override
    public ItemStack getStack(int slot) {
        InventoryComponent comp = getCompFromSlot(slot);
        return comp.equals(null) ? ItemStack.EMPTY : comp.getStack(tempSlot);
    }

    @Override
    public List<ItemStack> getStacks() {
		List<ItemStack> ret = new ArrayList<>();
        for (InventoryComponent comp : childComps) ret.addAll(comp.getStacks());
        return ret;
    }

    @Override
    public ItemStack insertStack(ItemStack stack, ActionType action) {
        for (InventoryComponent comp : childComps) stack = comp.insertStack(stack, action);
        return stack;
    }

    @Override
    public ItemStack insertStack(int slot, ItemStack stack, ActionType action) {
        InventoryComponent comp = getCompFromSlot(slot);
        return (comp.equals(null)) ? stack : comp.insertStack(tempSlot, stack, action);
    }

    //XXX: why not also make search for a stack possible?
    @Override
    public ItemStack removeStack(int slot, ActionType action) {
        InventoryComponent comp = getCompFromSlot(slot);
        return (comp.equals(null)) ? ItemStack.EMPTY : comp.removeStack(tempSlot, action);
    }

    @Override
    public ItemStack removeStack(int slot, int amount, ActionType action) {
        InventoryComponent comp = getCompFromSlot(slot);
        return (comp.equals(null)) ? ItemStack.EMPTY : comp.removeStack(tempSlot, amount, action);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        InventoryComponent comp = getCompFromSlot(slot);
        if (!(comp.equals(null))) comp.setStack(tempSlot, stack);
    }

	@Override
	public void clear() {
		for (InventoryComponent comp : childComps) comp.clear();
	}

	@Override
	public int getMaxStackSize(int slot) {
        InventoryComponent comp = getCompFromSlot(slot);
        return (comp.equals(null)) ? 0 : comp.getMaxStackSize(slot);
	}

	@Override
	public boolean isAcceptableStack(int slot, ItemStack stack) {
        InventoryComponent comp = getCompFromSlot(slot);
        return (comp.equals(null)) ? false : comp.isAcceptableStack(slot, stack);
	}

	@Override
	public int amountOf(Set<Item> items) {
		int amount = 0;
        for (InventoryComponent comp : childComps) amount += comp.amountOf(items);
		return amount;
	}

    //XXX: is this actually needed?
	@Override
	public Inventory asInventory() {
		return null;
	}

	@Override
	public SidedInventory asLocalInventory(WorldAccess world, BlockPos pos) {
		return null;
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		ListTag compTag = new ListTag();
		for (InventoryComponent comp : childComps) compTag.add(comp.toTag(new CompoundTag()));
        tag.put("InventoryComponents", compTag);
        return tag;
	}
    
    @Override
	public void fromTag(CompoundTag tag) {
        ListTag compTag = tag.getList("InventoryComponents", NbtType.LIST);
        for (int i = 0; i < childComps.length; i++) {
            CompoundTag invTag = compTag.getCompound(i);
            childComps[i].fromTag(invTag);
        }
	}

    protected InventoryComponent getCompFromSlot(int slot) {
        for (InventoryComponent comp : childComps) if (comp.size() <= slot) slot -= comp.size() - 1;
        else {
            tempSlot = slot;
            return comp;
        }
        return null;
    }
}
