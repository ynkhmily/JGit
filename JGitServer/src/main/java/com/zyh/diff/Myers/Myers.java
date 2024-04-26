package com.zyh.diff.Myers;

import com.zyh.Constant.SysConstant;
import com.zyh.Exception.SysException;
import com.zyh.diff.Diff;
import com.zyh.diff.Line;
import com.zyh.diff.Mode;
import lombok.SneakyThrows;


import java.util.*;



public class Myers<T> implements Diff {

    private PathNode buildPath(List<T> orig,List<T> target) throws SysException {
        if(Objects.isNull(orig) || Objects.isNull(target)){
            throw new SysException("[Myers][buildPath] 入参不能为NULL");
        }

        final int N = orig.size();
        final int M = target.size();
        final int MAX = N + M + 1;
        final int SIZE = 1 + 2 * MAX;
        final int MID = SIZE / 2;
        final PathNode v[] = new PathNode[SIZE];    // v[k] 当前k下的最大x, k = i - j
        v[MID + 1] = new PathNode.Snake(0,-1,null);

        //d 为修改次数，k为差
        for(int d = 0;d < MAX;d ++){
            for(int k = -d;k <= d;k += 2){
                final int KMID = MID + k;
                final int KPLUS = KMID + 1;
                final int KMINUS = KMID - 1;
                int i;
                PathNode prev;
                // k = -d时只能从上往下走
                if((k == -d) || (k != d && v[KMINUS].i < v[KPLUS].i)){
                    i = v[KPLUS].i;
                    prev = v[KPLUS];
                } else {
                    // k = d时，只能从左往右走
                    i = v[KMINUS].i + 1;
                    prev = v[KMINUS];
                }

                int j = i - k;
                v[KMINUS] = null;
                PathNode node = new PathNode.DiffNode(i, j, prev);
                while(i < N && j < M && orig.get(i).equals(target.get(j))){
                    i ++;
                    j ++;
                }

                if(i > node.i){
                    node = new PathNode.Snake(i, j, node);
                }
                v[KMID] = node;

                if(i >= N && j >= M){
                    return v[KMID];
                }
            }
        }

        return null;
    }

    public List<Line> buildDiff(PathNode path, List<T> orig, List<T> rev) {
        List<Line> result = new ArrayList<>();
        if (path == null)
            throw new IllegalArgumentException("path is null");
        if (orig == null)
            throw new IllegalArgumentException("original sequence is null");
        if (rev == null)
            throw new IllegalArgumentException("revised sequence is null");
        while (path != null && path.prev != null && path.prev.j >= 0) {
            if (path.isSnake()) {
                int endi = path.i;
                int begini = path.prev.i;
                for (int i = endi - 1; i >= begini; i --) {
                    result.add(new Line(i,orig.get(i).toString(), Mode.ori));
                }
            } else {
                int i = path.i;
                int j = path.j;
                int prei = path.prev.i;
                if (prei < i) {
                    result.add(new Line(i - 1,orig.get(i - 1).toString(), Mode.delete));
                } else {
                    result.add(new Line(j - 1,rev.get(j - 1).toString(), Mode.insert));
                }
            }
            path = path.prev;
        }
        Collections.reverse(result);
        return result;
    }

    public static void main(String[] args) {
        String oldText = "A\nB\nC\nA\nB\nB\nA";
        String newText = "C\nB\nA\nB\nA\nC";
        List<String> oldList = Arrays.asList(oldText.split("\\n"));
        List<String> newList = Arrays.asList(newText.split("\\n"));
        Myers<String> myersDiff = new Myers<>();
        try {
            List<Line> lines = myersDiff.showDiff(oldList, newList);
            for (Line line : lines) {
                if(line.mode == Mode.insert){
                    System.out.print("+ ");
                } else if(line.mode == Mode.delete){
                    System.out.print("- ");
                } else{
                    System.out.print("  ");
                }
                System.out.println(line.getSource());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SneakyThrows
    public List<Line> showDiff(List ori, List target) {
        PathNode path = buildPath(ori, target);

        return buildDiff(path, ori, target);
    }
}
