package com.zyh.Store;

import com.zyh.Constant.SysConstant;
import com.zyh.Exception.SysException;
import com.zyh.Utils.FileUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Setter
@Getter
public class Commit extends StoreFile {

    public Entry child;

    public String msg;

    public List<String> parents;

    private Boolean isHead = false;

    public static final String HEAD = "HEAD";


    public Commit() {
        super(StoreType.commit);
        parents = new ArrayList<>();
    }

    public Commit(String msg, Entry child, List<String> parents){
        super(StoreType.commit);
        this.msg = msg;
        this.child = child;
        this.parents = parents;
    }

    @Override
    public void writeToFile(String filePath) throws SysException {
        try {
            String hash = getHash();

            String dir = hash.substring(0, 2);
            String fileName = hash.substring(2);
            FileUtils.mkdir(FileUtils.generateFilePath(filePath, dir), dir);
            File targetFile = new File(FileUtils.generateFilePath(filePath, dir, fileName));

            // 如果文件存在，则说明没有修改任何东西
            if (!targetFile.exists()) {
                String tmpFileName = FileUtils.generateTmpFileName();
                File tmpFile = new File(FileUtils.generateFilePath(filePath, dir, tmpFileName));
                RandomAccessFile accessFile = new RandomAccessFile(tmpFile, SysConstant.RW);

                write(accessFile);

                accessFile.close();
                tmpFile.renameTo(targetFile);

                // 如果是Head则在sysWorkspace再次写入
                if(isHead){
                    File file = new File(filePath);
                    String parentFilePath = file.getParent();
                    writeAsHead(parentFilePath);
                }
            }
        } catch (Exception e){
            throw new SysException("[writeToFile] 失败",e);
        }

    }

    /**
     * 调用该方法时原有的HEAD会被覆盖
     * @param parentFilePath
     */
    private void writeAsHead(String parentFilePath) throws Exception {
        File targetFile = new File(FileUtils.generateFilePath(parentFilePath, HEAD));

        if(targetFile.exists()){
            boolean res = targetFile.delete();
            if(!res){
                throw new SysException("[writeAsHead] 原HEAD文件删除失败");
            }
        }

        String tmpFileName = FileUtils.generateTmpFileName();
        File tmpFile = new File(FileUtils.generateFilePath(parentFilePath, tmpFileName));
        RandomAccessFile accessFile = new RandomAccessFile(tmpFile, SysConstant.RW);

        write(accessFile);

        accessFile.close();
        tmpFile.renameTo(targetFile);
    }

    private void write(RandomAccessFile accessFile) throws Exception{
        // 写类型
        accessFile.writeInt(type.code);

        int len = msg.length();
        accessFile.writeInt(len);
        accessFile.write(msg.getBytes());

        // 写child
        child.write(accessFile);

        // 写parents
        int parentsNum = parents.size();
        accessFile.writeInt(parentsNum);

        for (String parent : parents) {
            int parentLen = parent.length();
            accessFile.writeInt(parentLen);
            accessFile.write(parent.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void catFile() throws SysException {
        System.out.println("child: " + child.getHash());
        System.out.println("msg: " + msg);
        if(parents.size() > 0){
            StringBuilder parentsMsg = new StringBuilder("parents: [");
            for (int i = 0; i < parents.size(); i++) {
                parentsMsg.append(parents.get(i));
                if(i != parents.size() - 1){
                    parentsMsg.append(",");
                }
            }
            parentsMsg.append("]");
            System.out.println(parentsMsg);
        }
    }

    protected InputStream getContentStream(){
        byte[] bytes = child.toString().getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        return byteArrayInputStream;
    }

    public static Commit readFromFile(RandomAccessFile accessFile) throws Exception{
        accessFile.seek(0);
        // 读类型
        int typeCode = accessFile.readInt();

        // 读msg
        int msgLen = accessFile.readInt();
        byte[] msgBytes = new byte[msgLen];
        accessFile.read(msgBytes);
        String msg = new String(msgBytes,StandardCharsets.UTF_8);

        // 读child
        Entry child = Entry.read(accessFile);

        // 读parents
        List<String> parents = new ArrayList<>();
        int parentsNum = accessFile.readInt();
        for (int i = 0; i < parentsNum; i++) {
            int parentLen = accessFile.readInt();
            byte[] parentByte = new byte[parentLen];
            accessFile.read(parentByte);
            parents.add(new String(parentByte,StandardCharsets.UTF_8));
        }
        return new Commit(msg, child, parents);
    }

    public String getHash() throws SysException {
        InputStream inputStream = getContentStream();
        String hash = compress(inputStream);
        return hash;
    }

    /**
     * 从文件中加载上次commit快照下的所有文件
     * @return
     */
    public Map<String, Entry> load(String sysWorkspace) throws SysException{
        String childHash = child.getHash();
        File childFile = FileUtils.findStoreFile(FileUtils.generateFilePath(sysWorkspace, SysConstant.WORK_OBJECTS_DIR), childHash);
        Tree tree = (Tree) FileUtils.readFromStoreFile(childFile);

        TreeMap<String, Entry> data = new TreeMap<>();
        tree.load(sysWorkspace, data);

        return data;
    }

}
