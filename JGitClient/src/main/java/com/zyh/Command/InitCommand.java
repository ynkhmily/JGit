package com.zyh.Command;

import com.zyh.Exception.SysException;
import com.zyh.Utils.FileUtils;
import lombok.Data;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.zyh.Constant.SysConstant.*;
import static com.zyh.Constant.SysConstant.WORK_REFS_DIR;
import static com.zyh.Store.Index.generateIndexFile;

@Data
public class InitCommand implements Command{

    public static final Logger LOGGER = LoggerFactory.getLogger(InitCommand.class);

    private String usrWorkspace;

    private String sysWorkspace;

    public InitCommand(String usrWorkspace){
        this.usrWorkspace = usrWorkspace;
        this.sysWorkspace = FileUtils.generateFilePath(usrWorkspace, WORK_ROOT_DIR);
    }

    @Override
    public void exec(String[] args) {
        if(args.length != 1){
            LOGGER.error("[InitCommand][exec]未知的参数");
        }
        try {
            FileUtils.mkdir(sysWorkspace, WORK_ROOT_DIR);
            FileUtils.mkdir(FileUtils.generateFilePath(sysWorkspace, WORK_OBJECTS_DIR), WORK_OBJECTS_DIR);
            FileUtils.mkdir(FileUtils.generateFilePath(sysWorkspace, WORK_REFS_DIR), WORK_REFS_DIR);

        } catch (Exception e){
            LOGGER.error("[InitCommand][exec] 失败", e);
        }
    }
}
