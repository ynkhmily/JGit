package com.zyh.Command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

@Data
@AllArgsConstructor
public class Context {

    private Command command;

    @SneakyThrows
    public void exec(String[] args){
        command.exec(args);
    }
}
