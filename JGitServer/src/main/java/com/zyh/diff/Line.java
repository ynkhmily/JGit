package com.zyh.diff;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Line {
    public final int line;

    public final String source;

    public final Mode mode;
}
