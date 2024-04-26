package com.zyh.Command;

import com.zyh.Constant.SysConstant;
import com.zyh.Exception.SysException;
import com.zyh.Store.Commit;
import com.zyh.Store.StoreFile;
import com.zyh.Store.StoreType;
import com.zyh.Utils.FileUtils;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

/**
 * cat-file -t fileName 打印文件类型
 * cat-file -p fileName 打印文件内容
 */

public class CatFileCommand implements Command{

    public static final Logger LOGGER = LoggerFactory.getLogger(CatFileCommand.class);

    private String sysWorkspace;

    private String usrWorkspace;

    public CatFileCommand(String workspace){
        this.usrWorkspace = workspace;
        this.sysWorkspace = FileUtils.generateFilePath(workspace, SysConstant.WORK_ROOT_DIR, SysConstant.WORK_OBJECTS_DIR);
    }

    @Override
    public void exec(String[] args) {
        if(args.length != 3){
            LOGGER.error("[catFileCommand] 参数个数不匹配");
        }
        try {
            String fileName = args[2];
            File file = FileUtils.findStoreFile(sysWorkspace, fileName);

            if(fileName.equals(Commit.HEAD)){
                file = new File(FileUtils.generateFilePath(usrWorkspace, SysConstant.WORK_ROOT_DIR), Commit.HEAD);
            }

            if (Objects.isNull(file) || !file.exists()) {
                LOGGER.error("[catFileCommand] 未找到指定文件" + fileName);
            }
            if (args[1].equals("-t")) {
                StoreFile storeFile = FileUtils.readFromStoreFile(file);
                StoreType type = storeFile.getType();
                System.out.println(type.type);
            } else if (args[1].equals("-p")) {
                StoreFile storeFile = FileUtils.readFromStoreFile(file);
                storeFile.catFile();
            }
        } catch (SysException e){
            LOGGER.error("[catFileCommand] 失败",e);
        }
    }
}
