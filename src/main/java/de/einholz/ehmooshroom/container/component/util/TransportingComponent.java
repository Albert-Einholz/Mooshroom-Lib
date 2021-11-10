package de.einholz.ehmooshroom.container.component.util;

import de.einholz.ehmooshroom.container.component.config.SideConfigComponent;
import de.einholz.ehmooshroom.container.component.config.SideConfigComponent.SideConfigBehavior;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

//TODO delete T stuff
//T may be null if not applicable
public interface TransportingComponent<C extends TransportingComponent<C>> extends CustomComponent {
    //return null if not applicable
    SideConfigComponent getSideConfig();
    //Number getContent(T type);
    //Number getSpace(T type);
    Number getMaxTransfer();
    void setMaxTransfer(Number maxTransfer);
    //Number change(Number amount, Action action);
    //assumes transportation is allowed
    Number transport(C from, C to);

    //here Direction is always from the perspective of the block performing the action
    @SuppressWarnings("unchecked")
    default Number pull(C from, Direction dir) {
        if (!getSideConfig().allows(getId(), dir, SideConfigBehavior.SELF_INPUT) || !from.getSideConfig().allows(from.getId(), dir.getOpposite(), SideConfigBehavior.FOREIGN_INPUT)) return 0;
        return transport(from, (C) this);
    }

    @SuppressWarnings("unchecked")
    default Number push(C to, Direction dir) {
        if (!getSideConfig().allows(getId(), dir, SideConfigBehavior.SELF_OUTPUT) || !to.getSideConfig().allows(to.getId(), dir.getOpposite(), SideConfigBehavior.FOREIGN_OUTPUT)) return 0;
        return transport((C) this, to);
    }

    @SuppressWarnings("unchecked")
    default Number pull(C from) {
        return transport(from, (C) this);
    }

    @SuppressWarnings("unchecked")
    default Number push(C to) {
        return transport((C) this, to);
    }

    @Override
    default void writeNbt(NbtCompound tag) {
        tag.putFloat("Max_Transfer", getMaxTransfer().floatValue());
    }

    @Override
    default void readNbt(NbtCompound tag) {
        setMaxTransfer(tag.getFloat("Max_Transfer"));
    }
}
