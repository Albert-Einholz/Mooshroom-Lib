package de.einholz.ehmooshroom.container.component.energy;

import de.einholz.ehmooshroom.MooshroomLib;
import de.einholz.ehmooshroom.container.component.util.BarComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.util.Identifier;

public interface EnergyComponent extends BarComponent {
    public static final Identifier ENERGY_ID = MooshroomLib.HELPER.makeId("heat");
    public static final ComponentKey<EnergyComponent> ENERGY = ComponentRegistry.getOrCreate(ENERGY_ID, EnergyComponent.class);

    @Override
    default Identifier getId() {
        return ENERGY_ID;
    }

    @Override
    default float getMin() {
        return BarComponent.ZERO;
    }
}