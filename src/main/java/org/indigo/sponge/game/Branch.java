package org.indigo.sponge.game;

import java.util.List;

public final class Branch implements RoomNode {
    private final List<RoomNode> children;

    public Branch(List<RoomNode> children) {
        this.children = children;
    }

    public List<RoomNode> getChildren() {
        return children;
    }
}
