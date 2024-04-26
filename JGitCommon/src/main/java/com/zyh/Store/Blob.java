package com.zyh.Store;

import com.zyh.Constant.SysConstant;
import com.zyh.Exception.SysException;
import com.zyh.Utils.FileUtils;
import com.zyh.Utils.ZLibUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Objects;


@Setter
@Getter
public class Blob extends StoreFile{

    public static final Logger LOGGER = LoggerFactory.getLogger(Blob.class);

    /**
     * 用户空间的文件
     */
    private File sourceFile;

    /**
     * 系统管理的压缩文件
     */
    private File targetFile;

    private Boolean updated = false;

    public Blob(File sourceFile,File targetFile){
        super(StoreType.blob);
        this.sourceFile = sourceFile;
        this.targetFile = targetFile;
    }

    public Blob(){
        super(StoreType.blob);
    }

    @Override
    public void writeToFile(String filePath) throws SysException {
        try (FileInputStream fileInputStream = new FileInputStream(sourceFile)){

            String hash = compress(fileInputStream);
            String dir = hash.substring(0, 2);
            String fileName = hash.substring(2);
            FileUtils.mkdir(FileUtils.generateFilePath(filePath,dir),dir);
            File targetFile = new File(FileUtils.generateFilePath(filePath, dir, fileName));

            // 先放在临时文件下，写完后重命名
            if(!targetFile.exists()){
                String tmpFileName = FileUtils.generateTmpFileName();
                File tmpFile = new File(FileUtils.generateFilePath(filePath, dir, tmpFileName));
                RandomAccessFile accessFile = new RandomAccessFile(tmpFile, SysConstant.RW);

                write(sourceFile, accessFile);
                accessFile.close();
                tmpFile.renameTo(targetFile);

                this.targetFile = targetFile;
                updated = true;
            }

        } catch (Exception e) {
            throw new SysException("[writeToFile] 失败",e);
        }
    }

    @Override
    public void catFile() throws SysException {
        if(Objects.isNull(targetFile)){
            throw new SysException("[catFile] targetFile未指定");
        }
        try(RandomAccessFile accessFile = new RandomAccessFile(targetFile, SysConstant.RW)){
            // 读取文件类型
            int type = accessFile.readInt();
            StoreType storeType = StoreType.getInstance(type);
            if(storeType != StoreType.blob){
                throw new SysException("[catFile] 文件类型不匹配，预计文件类型为 blob");
            }

            String s = ZLibUtils.decompressData(accessFile);
            System.out.println(s);
        } catch (Exception e) {
            throw new SysException("[catFile] 失败",e);
        }
    }

    public void write(File sourceFile, RandomAccessFile file) throws IOException, SysException {
        try(FileInputStream fileInputStream = new FileInputStream(sourceFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);) {
            byte[] buf = new byte[SysConstant.BUFFER_SIZE];

            // 文件类型
            file.writeInt(type.code);

            int total = 0;
            // 写入文件内容
            while ((total = bufferedInputStream.read(buf)) != -1) {
                // total 防止数据不足一个BUFFER_SIZE
                byte[] compressData = ZLibUtils.compress(buf, total);
                // 添加到压缩文件中
                file.write(compressData);
            }
        }
    }

    public String getHashByFileName() throws SysException {
        if(Objects.isNull(targetFile)){
            throw new SysException("[getHash] 未指定文件");
        }
        String name = targetFile.getName();
        String parent = targetFile.getParent();
        File parentFile = new File(parent);
        String dir = parentFile.getName();

        return dir + name;
    }

    public String getContentHash() throws SysException {
        try (FileInputStream fileInputStream = new FileInputStream(sourceFile)) {
            String hash = compress(fileInputStream);
            return hash;
        } catch (Exception e){
            throw new SysException("[getContentHash] 失败",e);
        }
    }
}
