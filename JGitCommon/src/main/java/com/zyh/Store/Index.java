package com.zyh.Store;

import com.zyh.Constant.SysConstant;
import com.zyh.Exception.SysException;
import com.zyh.Utils.FileUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;

@Getter
public class Index extends StoreFile{

    private Map<String, Entry> data;

    private File file;

    private Index(File file) throws SysException {
        super(StoreType.index);
        this.file = file;
        data = new TreeMap<>();
    }

    @Override
    public void writeToFile(String filePath) throws SysException {
        try(RandomAccessFile accessFile = new RandomAccessFile(file, SysConstant.RW);){
            // 写类型
            accessFile.writeInt(type.code);

            // 写条数
            int size = data.size();
            accessFile.writeInt(size);

            // 写内容
            for (Entry e : data.values()) {
                e.write(accessFile);
            }
        } catch (Exception e){
            throw new SysException("[writeToFile] index写入失败");
        }
    }

    @Override
    public void catFile() {
        for (Entry entry : data.values()) {
            System.out.println(entry);
        }
    }

    public static Index generateIndexFile(String sysWorkspace) throws SysException {
        String filePath = FileUtils.generateFilePath(sysWorkspace, StoreType.index.type);
        File file = new File(filePath);
        Index index = new Index(file);
        try {
            if (!file.exists()) {
                file.createNewFile();
            } else {
                index.loadData();
            }
        } catch (Exception e){
            throw new SysException("[generateIndexFile] 创建Index失败",e);
        }
        return index;
    }

    private void loadData() throws SysException {
        try(RandomAccessFile accessFile = new RandomAccessFile(file, SysConstant.RW)){
            int typeCode = accessFile.readInt();
            int len = accessFile.readInt();

            for (int i = 0; i < len; i++) {
                Entry entry = Entry.read(accessFile);
                data.put(entry.getFilePath(), entry);
            }
        } catch (Exception e){
            throw new SysException("[loadData] index写入失败");
        }
    }

    public void update(Map<String, Entry> newData){
        Set<Map.Entry<String, Entry>> entries = newData.entrySet();
        for (Map.Entry<String, Entry> entry : entries) {
            String key = entry.getKey();
            data.put(key, entry.getValue());
        }
    }

    public boolean containsFile(String filePath){

        return data.containsKey(filePath);
    }

    public void setDelete(String filePath) {
        Entry entry = data.get(filePath);
        entry.setDeleted(true);
        data.put(filePath, entry);
    }

    public void batchSetDelete(Set<String> existFiles){
        Set<String> keySet = data.keySet();
        for (String key : keySet) {
            if(!existFiles.contains(key)){
                Entry entry = data.get(key);
                entry.setDeleted(true);
                data.put(key, entry);
            }
        }
    }

    public boolean dataIsEmpty(){
        return data.isEmpty();
    }
}
