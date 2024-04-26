package com.zyh.Exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class SysException extends Exception {

    public String msg;

    public Exception e;

    public SysException(String msg){
        this.msg = msg;
    }

    @Override
    public String toString(){
        if(Objects.isNull(e)) {
            return "[msg]" + msg;
        }
        return "[msg]" + msg + ", " + "[exception]" + e;
    }

}
