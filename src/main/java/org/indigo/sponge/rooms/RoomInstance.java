package org.indigo.sponge.rooms;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class RoomInstance {
    private RoomTemplate roomTemplate;
    private Location pasteLoc;

    public RoomInstance(RoomTemplate roomTemplate){
        this.roomTemplate = roomTemplate;
    }

    public void generate(Location pasteLoc, int rotation){
        this.pasteLoc = pasteLoc;

        File file = Path.of("room_templates/" + roomTemplate.name + ".schem").toFile();
        Clipboard clipboard;
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ClipboardHolder holder = new ClipboardHolder(clipboard);
        if (rotation != 0) {
            holder.setTransform(new AffineTransform().rotateY(rotation));
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(pasteLoc.getWorld()))) {
            Operation operation = holder
                    .createPaste(editSession)
                    .to(BlockVector3.at(pasteLoc.x(),pasteLoc.y(),pasteLoc.z()))
                    .build();
            Operations.complete(operation);
        } catch (Exception e){
            throw new RuntimeException(e);
        }


    }
}
