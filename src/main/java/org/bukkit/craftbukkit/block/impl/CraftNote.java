/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftNote extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.NoteBlock, org.bukkit.block.data.Powerable {

    public CraftNote() {
        super();
    }

    public CraftNote(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftNoteBlock

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.Instrument> INSTRUMENT = getEnum(net.minecraft.world.level.block.NoteBlock.class, "instrument", org.bukkit.Instrument.class);
    private static final net.minecraft.world.level.block.state.properties.IntegerProperty NOTE = getInteger(net.minecraft.world.level.block.NoteBlock.class, "note");

    @Override
    public org.bukkit.Instrument getInstrument() {
        return get(INSTRUMENT);
    }

    @Override
    public void setInstrument(org.bukkit.Instrument instrument) {
        set(INSTRUMENT, instrument);
    }

    @Override
    public org.bukkit.Note getNote() {
       return new org.bukkit.Note(get(NOTE));
    }

    @Override
    public void setNote(org.bukkit.Note note) {
        set(NOTE, (int) note.getId());
    }

    // org.bukkit.craftbukkit.block.data.CraftPowerable

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty POWERED = getBoolean(net.minecraft.world.level.block.NoteBlock.class, "powered");

    @Override
    public boolean isPowered() {
        return get(POWERED);
    }

    @Override
    public void setPowered(boolean powered) {
        set(POWERED, powered);
    }
}
