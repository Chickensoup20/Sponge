package org.indigo.sponge.block;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.indigo.sponge.Colors;
import org.indigo.sponge.Sponge;

import java.util.ArrayList;
import java.util.List;

public class CustomBlock {
    public static int noteCounter = 0;
    public static int instrumentIndex = 0;
    public static final List<Instrument> INSTRUMENTS = List.of(Instrument.values());
    public static List<CustomBlock> customBlocks = new ArrayList<>();

    private NoteBlock noteBlock = (NoteBlock) Bukkit.createBlockData(Material.NOTE_BLOCK);
    private String name;

    public CustomBlock(String name) {
        if (noteCounter > 24) {
            noteCounter = 0;
            instrumentIndex++;
        }

        if (instrumentIndex >= INSTRUMENTS.size()) {
            throw new IllegalStateException("Exceeded maximum available unique Note Block combinations!");
        }

        Instrument currentInstrument = INSTRUMENTS.get(instrumentIndex);
        noteBlock.setInstrument(currentInstrument);
        noteBlock.setNote(new Note(noteCounter));

        this.name = name;
        customBlocks.add(this);
        noteCounter++;
    }

    public NoteBlock getNoteBlock(){
        return noteBlock;
    }

    public ItemStack getItem(){
        ItemStack item = new ItemStack(Material.NOTE_BLOCK);
        BlockDataMeta meta = (BlockDataMeta) item.getItemMeta();
        meta.setBlockData(noteBlock);
        meta.displayName(Sponge.mm.deserialize(Colors.toMM(Colors.ROSE) + name));
        item.setItemMeta(meta);
        return item;
    }

    public static CustomBlock getInstance(Block block){
        if(block.getType() == Material.NOTE_BLOCK){
            for(CustomBlock customBlock : customBlocks){
                if(customBlock.noteBlock.equals(block.getBlockData())){
                    return customBlock;
                }
            }
        }
        return null;
    }


}
