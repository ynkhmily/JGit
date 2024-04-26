package com.zyh.Store;

import com.zyh.Exception.SysException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum StoreType {
    blob(0,"blob"),
    tree(1,"tree"),
    commit(2,"commit"),
    index(3,"index");

    public Integer code;

    public String type;

    public static StoreType getInstance(Integer code) throws SysException {
        if(code.equals(0)){
            return blob;
        } else if(code.equals(1)){
            return tree;
        } else if(code.equals(2)){
            return commit;
        } else if(code.equals(3)){
            return index;
        }

        throw new SysException("[StoreType] 未知的文件类型");
    }
}
