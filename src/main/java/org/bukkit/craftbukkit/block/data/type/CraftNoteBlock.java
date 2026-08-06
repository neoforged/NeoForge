package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftNoteBlock extends CraftBlockData implements NoteBlock {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, org.bukkit.Instrument> INSTRUMENT = getEnum("instrument", org.bukkit.Instrument.class);
    private static final net.minecraft.world.level.block.state.properties.IntegerProperty NOTE = getInteger("note");

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
}
