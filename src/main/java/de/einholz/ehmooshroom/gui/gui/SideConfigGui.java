package de.einholz.ehmooshroom.gui.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import de.einholz.ehmooshroom.MooshroomLib;
import de.einholz.ehmooshroom.gui.widget.Button;
import de.einholz.ehmooshroom.storage.SideConfigType;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SideConfigGui extends ContainerGui {
    protected WLabel acc;
    protected WListPanel<Identifier, ConfigEntry> configPanel;
    protected List<Identifier> configIds;
    // FIXME check this class for content that can be removed
    protected Map<Integer, ConfigButton> configButtons = new HashMap<Integer, ConfigButton>();
    protected Button cancel;

    // FIXME buttons and/or labels are misaligned
    protected SideConfigGui(ScreenHandlerType<? extends SyncedGuiDescription> type, int syncId, PlayerInventory playerInv, PacketByteBuf buf) {
        super(type, syncId, playerInv, buf);
    }

    @SuppressWarnings("unchecked")
    public static SideConfigGui init(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        return init(new SideConfigGui((ScreenHandlerType<SyncedGuiDescription>) Registry.SCREEN_HANDLER.get(MooshroomLib.HELPER.makeId("side_config")), syncId, playerInventory, buf));
    }

    public static SideConfigGui init(SideConfigGui gui) {
        gui.acc = new WLabel(new TranslatableText("block.ehmooshroom.side_config.acc"));
        gui.configIds = gui.getStorageMgr().getIds();
        // XXX: WHY DOES ConfigEntry::gui.new NOT WORK??? THIS REALLY SHOULD WORK!!!
        gui.configPanel = new WListPanel<>(gui.configIds, () -> gui.new ConfigEntry(), (id, entry) -> entry.build(id));
        gui.cancel = (Button) new Button((player) -> {
            if (player.world.getBlockEntity(gui.POS) instanceof NamedScreenHandlerFactory screenFactory) {
                if (!player.world.isClient) player.openHandledScreen(screenFactory);
                return true;
            }
            return false;
        }).setLabel(new LiteralText("X"));
        return (SideConfigGui) ContainerGui.init(gui);
    }

    @Override
    protected void initWidgets() {
        super.initWidgets();
        /*
        for (Identifier id : getStorageMgr().getIds()) for (SideConfigType type : SideConfigType.values()) {
            ConfigButton button = new ConfigButton(buttonExes.size(), id, type);
            buttonExes.add(button);
            configButtons.put(button.buttonId, button);
            if (!getStorageMgr().getStorageEntry(id).available(type)) button.setEnabled(false);
            else button.setOnClick(getDefaultOnButtonClick(button));
        }
        */
        configPanel.setSize(9, 5);
        // TODO uncomment scrollPanel.setScrollingHorizontally(TriState.FALSE);
        cancel.tooltips.add("tooltip.ehmooshroom.cancel_button");
        addButton(cancel);
    }

    @Override
    protected void drawDefault() {
        super.drawDefault();
        ((WGridPanel) rootPanel).add(acc, 2, 1, 7, 1);
        ((WGridPanel) rootPanel).add(configPanel, 0, 2, 9, 5);
        ((WGridPanel) rootPanel).add(cancel, 9, 5, 1, 1);
    }

    protected class ConfigEntry extends WGridPanel {
        public ConfigEntry() {
            super(9);
        }

        public void build(Identifier id) {
            add(new WLabel(new TranslatableText("block." + id.getNamespace() + ".config." + id.getPath())), 0, 0, 4, 2);
            for (SideConfigType type : SideConfigType.values()) {
                ConfigButton button = new ConfigButton(getButtonAmount(), id, type);
                addButton(button);
                configButtons.put(getButtonIndex(button), button);
                if (!getStorageMgr().getEntry(id).available(type)) button.setEnabled(false);
                final int ACC_NO = button.type.ACC.ordinal();
                add(button, (ACC_NO > 1 ? 2 * (ACC_NO - 1) : ACC_NO) + 4 + (button.type.FOREIGN ? 1 : 0), button.type.OUTPUT ? 1 : 0);
            }
        }
    }

    protected class ConfigButton extends Button {
        public final int buttonId;
        public final Identifier storageId;
        public final SideConfigType type;

        @SuppressWarnings("unchecked")
        public ConfigButton(int buttonId, Identifier storageId, SideConfigType type) {
            super();
            setExe((player) -> {
                if (!isEnabled()) return false;
                getStorageMgr().getEntry(storageId).change(type);
                return true;
            });
            this.buttonId = buttonId;
            this.storageId = storageId;
            this.type = type;
            setSize(8, 8);
            if (!isEnabled()) return;
            Supplier<?>[] suppliers = {
                () -> {
                    return type.name().toLowerCase();
                }, () -> {
                    return type.ACC.toString();
                }, () -> {
                    return String.valueOf(getStorageMgr().getEntry(storageId).allows(type));
                }, () -> {
                    return String.valueOf(buttonId);
                }
            };
            advancedTooltips.put("tooltip.ehmooshroom.config_button", (Supplier<Object>[]) suppliers);
        }

        @Override
        public void draw(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
            if (isEnabled())
                withTint(getStorageMgr().getEntry(storageId).allows(type) ? 0xFFFFFF00 : 0xFFFF0000);
            else advancedTooltips.remove("tooltip.ehmooshroom.config_button");
            super.draw(matrices, x, y, mouseX, mouseY);
        }

        @Override
        public boolean canResize() {
            return false;
        }
    }
}
