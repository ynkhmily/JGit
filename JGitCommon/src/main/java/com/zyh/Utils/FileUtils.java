package com.zyh.Utils;

import com.zyh.Constant.SysConstant;
import com.zyh.Exception.SysException;
import com.zyh.Store.*;
import lombok.SneakyThrows;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.UUID;


public class FileUtils {
    public static String generateFilePath(String ... names){
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < names.length; i++) {
            res.append(names[i]);
            if(i != names.length){
                res.append(File.separator);
            }
        }

        return res.toString();
    }

    public static void mkdir(String path,String name) throws SysException {
        File file = new File(path);
        if(!file.exists()) {
            boolean res = file.mkdir();
            file.setWritable(true);
            if(!res){
                throw new SysException("[FileUtils][mkdir] 创建文件: " + name + "失败");
            }
        }
    }

    public static String generateTmpFileName(){
        UUID uuid = UUID.randomUUID();
        String res = uuid.toString().replace("-","").toLowerCase();

        return res;
    }

    /**
     * 查找工作目录下被压缩的文件
     * @param filePath
     * @param fileName
     * @return
     */
    public static File findStoreFile(String filePath,String fileName){
        String dir = fileName.substring(0, 2);
        String realFileName = fileName.substring(2);
        File file = new File(FileUtils.generateFilePath(filePath, dir, realFileName));
        if(!file.exists()){
            return null;
        }
        return file;
    }


    public static StoreFile readFromStoreFile(File file) throws SysException {
        if(!file.exists()){
            return null;
        }

        try(RandomAccessFile accessFile = new RandomAccessFile(file, SysConstant.RW)){
            int typeCode = accessFile.readInt();
            StoreType storeType = StoreType.getInstance(typeCode);
            // TODO 扩展
            if(storeType == StoreType.blob){
                return new Blob(null,file);
            } else if(storeType == StoreType.tree){
                return Tree.readFromFile(accessFile);
            } else if(storeType == StoreType.commit){
                return Commit.readFromFile(accessFile);
            }

            return null;
        } catch (Exception e) {
            throw new SysException("[readFromStoreFile] 失败，文件名" + file.getName(), e);
        }
    }

    // TODO 扩展成从文件中读取
    public static boolean filter(String fileName){
        if(fileName.equals(SysConstant.WORK_ROOT_DIR)){
            return true;
        }
        return false;
    }
}
