package com.zyh.Command;

import com.zyh.Constant.SysConstant;
import com.zyh.Exception.SysException;
import com.zyh.Store.Index;
import com.zyh.Utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LsFileCommand implements Command{

    public static final Logger LOGGER = LoggerFactory.getLogger(LsFileCommand.class);

    private String sysWorkspace;

    public LsFileCommand(String userWorkspace){
        this.sysWorkspace = FileUtils.generateFilePath(userWorkspace, SysConstant.WORK_ROOT_DIR);
    }

    @Override
    public void exec(String[] args) {
        if(args.length != 1){
            LOGGER.error("参数个数不匹配");
        }
        try {
            Index index = Index.generateIndexFile(sysWorkspace);
            index.catFile();
        } catch (SysException e){
            LOGGER.error("[LsFileCommand][exec] 失败",e);
        }
    }
}
