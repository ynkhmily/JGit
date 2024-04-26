package com.zyh.Store;

import com.zyh.Constant.SysConstant;
import com.zyh.Exception.SysException;
import com.zyh.Utils.FileUtils;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;



public class Tree extends StoreFile {
    // 文件夹名
    private String dir;

    private List<Entry> children;

    public Tree(String dir) {
        super(StoreType.tree);
        this.dir = dir;
        children = new ArrayList<>();
    }

    public Tree(String dir,List<Entry> children){
        super(StoreType.tree);
        this.dir = dir;
        this.children = children;
    }

    @Override
    public void writeToFile(String filePath) throws SysException {
        try {
            String hash = getHash();

            String dir = hash.substring(0, 2);
            String fileName = hash.substring(2);
            FileUtils.mkdir(FileUtils.generateFilePath(filePath, dir), dir);
            File targetFile = new File(FileUtils.generateFilePath(filePath, dir, fileName));

            if (!targetFile.exists()) {
                String tmpFileName = FileUtils.generateTmpFileName();
                File tmpFile = new File(FileUtils.generateFilePath(filePath, dir, tmpFileName));
                RandomAccessFile accessFile = new RandomAccessFile(tmpFile, SysConstant.RW);

                write(accessFile);

                accessFile.close();
                tmpFile.renameTo(targetFile);

            }
        } catch (Exception e){
            throw new SysException("[writeToFile] 失败",e);
        }
    }

    @Override
    public void catFile() {
        for (Entry child : children) {
            System.out.println(child.toString());
        }
    }

    private InputStream getContentStream(){
        StringBuilder res = new StringBuilder();
        for (Entry child : children) {
            res.append(child.toString());
        }
        byte[] bytes = res.toString().getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        return byteArrayInputStream;
    }

    private void write(RandomAccessFile accessFile) throws Exception {
        // 写头信息
        // 类型
        accessFile.writeInt(type.code);
        // 写当前代表的目录(可能为默认目录)
        int dirLen = dir.length();
        accessFile.writeInt(dirLen);
        accessFile.write(dir.getBytes(StandardCharsets.UTF_8));

        // 条数
        int size = children.size();
        accessFile.writeInt(size);

        // 写内容
        for (Entry child : children) {
            child.write(accessFile);
        }
    }
    
    public static Tree readFromFile(RandomAccessFile accessFile) throws Exception{
        // 设置为文件开始
        accessFile.seek(0);
        int typeCode = accessFile.readInt();
        int dirLen = accessFile.readInt();

        byte[] byteDir = new byte[dirLen];
        accessFile.read(byteDir);
        String dir = new String(byteDir,StandardCharsets.UTF_8);

        int childNum = accessFile.readInt();
        ArrayList<Entry> children = new ArrayList<>();
        for (int i = 0; i < childNum; i++) {
            Entry entry = Entry.read(accessFile);
            children.add(entry);
        }

        return new Tree(dir,children);
    }

    public String getHash() throws SysException {
        InputStream inputStream = getContentStream();
        String hash = compress(inputStream);
        return hash;
    }

    public void addChild(Entry entry){
        // 过滤
        String filePath = entry.getFilePath();
        for (Entry child : children) {
            if(child.getFilePath().equals(filePath)){
                return;
            }
        }

        this.children.add(entry);
    }

    /**
     *
     * @param map
     * @param filePath 完整的相对路径
     */
    public void completeEntryHash(Map<String, Tree> map,String filePath) throws SysException {
        for (Entry child : children) {
            String hash = child.getHash();
            if(Objects.isNull(hash)){
                String dir = child.getFilePath();
                String childFilePath = FileUtils.generateFilePath(filePath, dir);
                if(!map.containsKey(childFilePath)){
                    throw new SysException("[completeEntryHash] 异常，找不到指定文件" + childFilePath);
                }
                Tree tree = map.get(childFilePath);
                child.setHash(tree.getHash());
            }
        }
    }

    public String getDir(){
        return dir;
    }

    /**
     * 根据当前Tree加载所有文件的信息
     * @return
     */
    public void load(String sysWorkspace, Map<String, Entry> data) throws SysException {
        for (Entry child : children) {
            String hash = child.getHash();
            File file = FileUtils.findStoreFile(FileUtils.generateFilePath(sysWorkspace, SysConstant.WORK_OBJECTS_DIR), hash);
            try {
                StoreFile storeFile = FileUtils.readFromStoreFile(file);
                if(storeFile.getType() == StoreType.blob){
                    data.put(child.getFilePath(), child);
                } else if(storeFile.getType() == StoreType.tree){
                    Tree tree = (Tree) storeFile;
                    tree.load(sysWorkspace, data);
                }
            } catch (Exception e){
                throw new SysException("[load] 失败",e);
            }

        }

    }
}
