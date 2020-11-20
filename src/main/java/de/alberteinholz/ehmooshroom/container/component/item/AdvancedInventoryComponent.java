package de.alberteinholz.ehmooshroom.container.component.item;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import de.alberteinholz.ehmooshroom.MooshroomLib;
import de.alberteinholz.ehmooshroom.container.component.TransportingComponent;
import de.alberteinholz.ehmooshroom.container.component.data.ConfigDataComponent;
import de.alberteinholz.ehmooshroom.container.component.data.ConfigDataComponent.ConfigBehavior;
import de.alberteinholz.ehmooshroom.container.component.item.AdvancedInventoryComponent.Slot.Type;
import de.alberteinholz.ehmooshroom.util.Helper;
import io.github.cottonmc.component.api.ActionType;
import io.github.cottonmc.component.compat.vanilla.InventoryWrapper;
import io.github.cottonmc.component.compat.vanilla.SidedInventoryWrapper;
import io.github.cottonmc.component.item.InventoryComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class AdvancedInventoryComponent implements InventoryComponent, TransportingComponent<InventoryComponent> {
    protected Identifier id;
    //protected final InventoryWrapper inventoryWrapper = new InventoryWrapper(this);
    protected DefaultedList<Slot> slots;
    protected int maxTransfer;
    protected final List<Runnable> listeners = new ArrayList<>();
    protected ConfigDataComponent config;

    public AdvancedInventoryComponent(Identifier id, Type[] types, String defaultNamespace, String[] paths) {
        this(1, id, types, defaultIds(defaultNamespace, paths));
    }

    public AdvancedInventoryComponent(int maxTransfer, Identifier id, Type[] types, String defaultNamespace, String[] paths) {
        this(maxTransfer, id, types, defaultIds(defaultNamespace, paths));
    }

    public AdvancedInventoryComponent(Identifier id, Type[] types, Identifier[] ids) {
        this(1, id, types, ids);
    }

    //null id for no id
    //futher aperance of duplicate id will repleced with null
    //types determine size
    public AdvancedInventoryComponent(int maxTransfer, Identifier id, Type[] types, Identifier[] ids) {
        this.id = id;
        this.maxTransfer = maxTransfer;
        slots = DefaultedList.ofSize(types.length, new Slot());
        for (int i = 0; i < types.length; i++) {
            if (isIdPresent(ids[i])) warnDuplicteId(ids[i]);
            if (!types[i].equals(Type.STORAGE) && types[i] != null) slots.get(i).type = types[i];
            if (ids[i] != null) slots.get(i).id = ids[i];
        }
    }

    public void addSlots(Type[] types, String defaultNamespace, String[] paths) {
        addSlots(types, defaultIds(defaultNamespace, paths));
    }

    //null id for no id
    //types determine size
    public void addSlots(Type[] types, Identifier[] ids) {
        for (int i = 0; i < types.length; i++) {
            if (isIdPresent(ids[i])) warnDuplicteId(ids[i]);
            slots.add(new Slot(types[i], ids[i]));
        }
    }

    protected void warnDuplicteId(Identifier id) {
        MooshroomLib.LOGGER.smallBug(new IllegalArgumentException("There mustn't be duplicate ids. " + id + " will be repleced will null."));
        id = null;
    }

    public boolean isIdPresent(Identifier id) {
        for (Slot slot : slots) if (slot.id.equals(id)) return true;
        return false;
    }

    protected static Identifier[] defaultIds(String defaultNamespace, String[] paths) {
        Identifier[] ids = new Identifier[paths.length];
        for (int i = 0; i < paths.length; i++) ids[i] = new Identifier(defaultNamespace, paths[i]);
        return ids;
    }

    @Override
    public List<Runnable> getListeners() {
        return listeners;
    }

    @Override
    public void setConfig(ConfigDataComponent config) {
        this.config = config;
    }

    //XXX: Remove on UC update
    @Deprecated
    @Override
	public int getSize() {
		return size();
    }

	public int size() {
		return slots.size();
    }

	@Override
	public List<ItemStack> getStacks() {
		List<ItemStack> list = new ArrayList<>();
		for (Slot slot : slots) list.add(slot.stack.copy());
		return list;
	}

    @Deprecated
	@Override
	public DefaultedList<ItemStack> getMutableStacks() {
		DefaultedList<ItemStack> list = DefaultedList.ofSize(slots.size(), ItemStack.EMPTY);
		for (Slot slot : slots) list.add(slot.stack);
		return list;
    }
    
    //returns -1 if id is not present
    public int getIntFromId(Identifier id) {
        for (int i = 0; i < slots.size(); i++) if (slots.get(i).id.equals(id)) return i;
        return -1;
    }

	@Override
	public ItemStack getStack(int slot) {
		return slots.get(slot).stack.copy();
    }
    
    public List<Slot> getSlots(Type type) {
		List<Slot> list = new ArrayList<>();
		for (Slot slot : slots) if (slot.type.equals(type)) list.add(slot);
        return list;
    }
    
    public List<Integer> getInsertable() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) if (slots.get(i).type.insert) list.add(i);
        return list;
    }
    
    public List<Integer> getExtractable() {
		List<Integer> list = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) if (slots.get(i).type.extract) list.add(i);
        return list;
    }

    protected Type getType(int slot) {
        return slots.get(slot).type;
    }

    /*
    public static int move(Inventory from, Inventory to, int maxTransfer, Direction dir, ActionType action) {
        int transfer = 0;
        InventoryComponent fromComponent = from instanceof InventoryWrapper && ((InventoryWrapper) from).getComponent() != null ? ((InventoryWrapper) from).getComponent() : null;
        AdvancedInventoryComponent fromContainerComponent = fromComponent instanceof AdvancedInventoryComponent ? (AdvancedInventoryComponent) fromComponent : null;
        InventoryComponent toComponent = to instanceof InventoryWrapper && ((InventoryWrapper) to).getComponent() != null ? ((InventoryWrapper) to).getComponent() : null;
        AdvancedInventoryComponent toContainerComponent = toComponent instanceof AdvancedInventoryComponent ? (AdvancedInventoryComponent) toComponent : null;
        for (int idFrom : fromContainerComponent != null ? (Integer[]) fromContainerComponent.getExtractable().toArray() : ArrayUtils.toObject(Helper.countingArray(from.size()))) {
            if (fromContainerComponent != null ? fromContainerComponent.canExtract(idFrom, dir) : fromComponent != null ? fromComponent.canExtract(idFrom) : true) {
                //XXX: Update on UC update
                ItemStack extractionTest = fromComponent != null ? fromComponent.takeStack(idFrom, maxTransfer - transfer, ActionType.TEST) : from.getStack(idFrom).copy();
                if (extractionTest.isEmpty()) continue;
                if (extractionTest.getCount() > maxTransfer - transfer) extractionTest.setCount(maxTransfer - transfer);
                for (int idTo : toContainerComponent != null ? (Integer[]) toContainerComponent.getInsertable().toArray() : ArrayUtils.toObject(Helper.countingArray(to.size()))) {
                    int insertionCount = extractionTest.getCount() - (toComponent != null ? toComponent.insertStack(idTo, extractionTest, action).getCount() : 0);
                    if (!(to instanceof InventoryWrapper)) { //vanilla
                        ItemStack target = to.getStack(idTo);
                        insertionCount = target.getCount() + extractionTest.getCount() > Math.min(target.getMaxCount(), to.getMaxCountPerStack()) ? Math.min(target.getMaxCount(), to.getMaxCountPerStack()) - target.getCount() : extractionTest.getCount();
                        if (action.shouldPerform()) target.increment(insertionCount);
                    }
                    if (insertionCount <= 0) continue;
                    //XXX: Update on UC update
                    int extractionCount = fromComponent != null ? fromComponent.takeStack(idFrom, insertionCount, action).getCount() : action.shouldPerform() ? from.removeStack(idFrom, insertionCount).getCount() : extractionTest.getCount();
                    transfer += extractionCount;
                    if (insertionCount != extractionCount) MooshroomLib.LOGGER.smallBug(new IllegalStateException("Item moving wasn't performed correctly. This could lead to item deletion.")); 
                    if (transfer >= maxTransfer) break;
                }
            }
            if (transfer >= maxTransfer) break;
        }
        return transfer;
    }
    */

    @Override
    public Number pull(InventoryComponent from, Direction dir, ActionType action) {
        int transfer = 0;
        for (int fromSlot : from instanceof AdvancedInventoryComponent ? ((AdvancedInventoryComponent) from).getExtractable().toArray(new Integer[0]) : ArrayUtils.toObject(Helper.countingArray(from.getSize()))) {
            if ((from instanceof AdvancedInventoryComponent && !((AdvancedInventoryComponent) from).canExtract(fromSlot, dir.getOpposite())) || !from.canExtract(fromSlot)) continue;
            ItemStack extractionTest = from.takeStack(fromSlot, maxTransfer - transfer, ActionType.TEST);
            if (extractionTest.isEmpty()) continue;
            if (extractionTest.getCount() > maxTransfer - transfer) extractionTest.setCount(maxTransfer - transfer);
            for (int toSlot : getInsertable().toArray(new Integer[0])) {
                int insertionCount = extractionTest.getCount() - (insertStack(toSlot, extractionTest, action).getCount());
                if (insertionCount <= 0) continue;
                int extractionCount = from.takeStack(fromSlot, insertionCount, action).getCount();
                transfer += extractionCount;
                if (insertionCount != extractionCount) MooshroomLib.LOGGER.smallBug(new IllegalStateException("Item pulling wasn't performed correctly. This could lead to item deletion.")); 
                if (transfer >= maxTransfer) break;
            }
            if (transfer >= maxTransfer) break;
        }
        return transfer;
    }

    @Override
    public Number push(InventoryComponent to, Direction dir, ActionType action) {
        int transfer = 0;
        for (int fromSlot : getExtractable().toArray(new Integer[0])) {
            if (!canExtract(fromSlot, dir.getOpposite()) || !canExtract(fromSlot)) continue;
            ItemStack extractionTest = takeStack(fromSlot, maxTransfer - transfer, ActionType.TEST);
            if (extractionTest.isEmpty()) continue;
            if (extractionTest.getCount() > maxTransfer - transfer) extractionTest.setCount(maxTransfer - transfer);
            for (int toSlot : to instanceof AdvancedInventoryComponent ? ((AdvancedInventoryComponent) to).getInsertable().toArray(new Integer[0]) : ArrayUtils.toObject(Helper.countingArray(to.getSize()))) {
                int insertionCount = extractionTest.getCount() - (to.insertStack(toSlot, extractionTest, action).getCount());
                if (insertionCount <= 0) continue;
                int extractionCount = takeStack(fromSlot, insertionCount, action).getCount();
                transfer += extractionCount;
                if (insertionCount != extractionCount) MooshroomLib.LOGGER.smallBug(new IllegalStateException("Item pushing wasn't performed correctly. This could lead to item deletion.")); 
                if (transfer >= maxTransfer) break;
            }
            if (transfer >= maxTransfer) break;
        }
        return transfer;
    }

    /*
    @Deprecated
    public static int moveOld(Inventory from, Inventory to, int maxTransfer, Direction dir, ActionType action) {
        int transfer = 0;
        InventoryComponent fromComponent = from instanceof InventoryWrapper && ((InventoryWrapper) from).component != null ? ((InventoryWrapper) from).component : null;
        ContainerInventoryComponent fromContainerComponent = fromComponent instanceof ContainerInventoryComponent ? (ContainerInventoryComponent) fromComponent : null;
        InventoryComponent toComponent = to instanceof InventoryWrapper && ((InventoryWrapper) to).component != null ? ((InventoryWrapper) to).component : null;
        ContainerInventoryComponent toContainerComponent = toComponent instanceof ContainerInventoryComponent ? (ContainerInventoryComponent) toComponent : null;
        for (Object idFrom : fromContainerComponent != null ? fromContainerComponent.getSlots(Type.OUTPUT).keySet().toArray() : Helper.countingArray(from.size())) {
            if (fromContainerComponent != null ? fromContainerComponent.canExtract((String) idFrom, dir) : fromComponent != null ? fromComponent.canExtract((int) idFrom) : true) {
                ItemStack extractionTest = fromContainerComponent != null ? fromContainerComponent.removeStack((String) idFrom, maxTransfer, ActionType.TEST) : fromComponent != null ? fromComponent.removeStack((int) idFrom, ActionType.TEST) : from.getStack((int) idFrom).copy();
                if (extractionTest.isEmpty()) continue;
                if (extractionTest.getCount() > maxTransfer - transfer) extractionTest.setCount(maxTransfer - transfer);
                for (Object idTo : toContainerComponent != null ? toContainerComponent.getSlots(Type.INPUT).keySet().toArray() : Helper.countingArray(to.size())) {
                    int insertionCount = extractionTest.getCount() - (toContainerComponent != null ? toContainerComponent.insertStack((String) idTo, extractionTest, action).getCount() : toComponent != null ? toComponent.insertStack((int) idTo, extractionTest, action).getCount() : 0);
                    if (!(to instanceof InventoryWrapper)) {
                        ItemStack gotten = to.getStack((int) idTo);
                        insertionCount = gotten.getCount() + extractionTest.getCount() > gotten.getMaxCount() || gotten.getCount() + extractionTest.getCount() > to.getMaxCountPerStack() ? Math.min(gotten.getMaxCount(), to.getMaxCountPerStack()) - gotten.getCount() : extractionTest.getCount();
                        if (action.shouldPerform()) gotten.increment(insertionCount);
                    }
                    if (insertionCount <= 0) continue;
                    int extractionCount = fromContainerComponent != null ? fromContainerComponent.removeStack((String) idFrom, insertionCount, action).getCount() : from.removeStack((int) idFrom, insertionCount).getCount();
                    transfer += extractionCount;
                    if (insertionCount != extractionCount) TechMod.LOGGER.smallBug(new IllegalStateException("Item moving wasn't performed correctly. This could lead to item deletion.")); 
                    if (transfer >= maxTransfer) break;
                }
            }
            if (transfer >= maxTransfer) break;
        }
        return transfer;
    }
    */

    public boolean canInsert(int slot, Direction dir) {
        if (!canInsert(slot)) return false;
        else  return ((ConfigDataComponent) config).allowsConfig(id, ConfigBehavior.FOREIGN_INPUT, dir);
    }

	@Override
	public boolean canInsert(int slot) {
        return getType(slot).insert;
	}

    public boolean canExtract(int slot, Direction dir) {
        if (!canExtract(slot)) return false;
        else return ((ConfigDataComponent) config).allowsConfig(id, ConfigBehavior.FOREIGN_OUTPUT, dir);
    }

	@Override
	public boolean canExtract(int slot) {
		return getType(slot).extract;
	}

    //XXX: Remove on UC update
    @Deprecated
    @Override
    public ItemStack takeStack(int slot, int amount, ActionType action) {
		return removeStack(slot, amount, action);
    }

    public ItemStack removeStack(int slot, int amount, ActionType action) {
		ItemStack stack = getStack(slot);
		if (action.shouldPerform()) onChanged();
		else stack = stack.copy();
        return stack.split(amount);
    }
    
    @Override
    public ItemStack removeStack(int slot, ActionType action) {
        if (action.shouldPerform()) {
            setStack(slot, ItemStack.EMPTY);
            onChanged();
        }
        return getStack(slot);
    }

    @Override
	public void setStack(int slot, ItemStack stack) {
		if (!isAcceptableStack(slot, stack)) {
            slots.get(slot).stack = stack;
            onChanged();
        }
    }

    @Override
    public ItemStack insertStack(int slot, ItemStack stack, ActionType action) {
		ItemStack target = getStack(slot);
        int maxSize = Math.min(target.getMaxCount(), getMaxStackSize(slot));
        if (target.getCount() >= maxSize) target.setCount(maxSize);
		if (!target.isEmpty() && !target.isItemEqualIgnoreDamage(stack) || target.getCount() >= maxSize || !isAcceptableStack(slot, stack)) return stack;
        if (!action.shouldPerform()) stack = stack.copy();
        else onChanged();
        ItemStack newTarget = stack.split(maxSize - target.getCount());
        if (target.isEmpty()) setStack(slot, newTarget);
        else target.increment(newTarget.getCount());
        return stack;
    }
    
	@Override
	public ItemStack insertStack(ItemStack stack, ActionType action) {
        for (int i = 0; i < size(); i++) {
            stack = insertStack(i, stack, action);
			if (stack.isEmpty()) return stack;
        }
		return stack;
    }

    @Override
    public boolean isAcceptableStack(int slot, ItemStack stack) {
        return true;
    }

    //TODO: make custom toTag & fromTag for not adding empty tags
    
    //XXX:currently not available will change back later
    /*
    public boolean containsInput(ItemIngredient ingredient) {
        int amount = 0;
        for (Slot slot : slots) {
            if (ingredient.ingredient != null && slot.type.equals(Type.INPUT) && ingredient.ingredient.contains(slot.stack.getItem()) && (ingredient.tag == null || NbtHelper.matches(ingredient.tag, slot.stack.getTag(), true))) amount += slot.stack.getCount();
            if (amount >= ingredient.amount) return true;
        }
        return false;
    }
    */

    //XXX: TEMP
    @Override
    public Inventory asInventory() {
        return InventoryWrapper.of(this);
    }

    //XXX: TEMP
    @Override
    public SidedInventory asLocalInventory(WorldAccess world, BlockPos pos) {
        return SidedInventoryWrapper.of(dir -> {
            return this;
        });
    }

    public boolean isSlotAvailable(int slot, Direction side) {
        return canInsert(slot, side) || canExtract(slot, side);
    }

    public static class Slot {
        public Type type;
        public Identifier id;
        public ItemStack stack = ItemStack.EMPTY;

        public Slot() {
            this(Type.STORAGE, null);
        }

        public Slot(Type type) {
            this(type, null);
        }

        public Slot(Type type, Identifier id) {
            this.type = type;
            this.id = id;
        }

        public enum Type {
            INPUT (true, false),
            OUTPUT (false, true),
            STORAGE (true, true),
            OTHER (false, false);

            public boolean insert;
            public boolean extract;

            private Type(boolean insert, boolean extract) {
                this.insert = insert;
                this.extract = extract;
            }
        }
    }
}