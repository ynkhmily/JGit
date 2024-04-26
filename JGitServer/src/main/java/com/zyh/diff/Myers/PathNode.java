package com.zyh.diff.Myers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class PathNode {
    public final int i;

    public final int j;

    public PathNode prev;

    public abstract boolean isSnake();

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("[");
        PathNode node = this;
        while (node != null) {
            buf.append("(");
            buf.append(Integer.toString(node.i));
            buf.append(",");
            buf.append(Integer.toString(node.j));
            buf.append(")");
            node = node.prev;
        }
        buf.append("]");
        return buf.toString();
    }

    static final class Snake extends PathNode{
        public Snake(int i,int j,PathNode prev){
            super(i,j,prev);
        }

        @Override
        public boolean isSnake() {
            return true;
        }
    }

    static final class DiffNode extends PathNode{
        public DiffNode(int i,int j,PathNode prev){
            super(i,j,prev);
        }

        @Override
        public boolean isSnake() {
            return false;
        }
    }
}
