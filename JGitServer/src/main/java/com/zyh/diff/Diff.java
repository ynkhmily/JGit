package com.zyh.diff;

import java.util.List;

public interface Diff<T> {

    List<Line> showDiff(List<T> ori, List<T> target);
}
