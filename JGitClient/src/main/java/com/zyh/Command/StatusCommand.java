package com.zyh.Command;

import com.zyh.Constant.SysConstant;
import com.zyh.Exception.SysException;
import com.zyh.Store.Blob;
import com.zyh.Store.Commit;
import com.zyh.Store.Entry;
import com.zyh.Store.Index;
import com.zyh.Utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class StatusCommand implements Command{

    public static final Logger LOGGER = LoggerFactory.getLogger(StatusCommand.class);

    private String usrWorkspace;

    private String sysWorkspace;

    private Index index;

    private Map<String, Entry> lastCommitTree;

    private Map<String, Entry> untrackFile;

    private Map<String, Entry> notInStageFile;

    private Map<String, String> stageFile;

    private Map<String, Entry>  usrWorkTree;

    public StatusCommand(String usrWorkspace){
        this.usrWorkspace = usrWorkspace;
        this.sysWorkspace = FileUtils.generateFilePath(usrWorkspace, SysConstant.WORK_ROOT_DIR);
        File headFile = new File(FileUtils.generateFilePath(sysWorkspace, Commit.HEAD));
        try(RandomAccessFile accessFile = new RandomAccessFile(headFile, SysConstant.RW)){
            index = Index.generateIndexFile(sysWorkspace);
            // 读取上次提交
            if(headFile.exists()) {
                Commit lastCommit = Commit.readFromFile(accessFile);
                lastCommitTree = lastCommit.load(sysWorkspace);
            }
        } catch (Exception e) {
            LOGGER.error("[StatusCommand] 初始化失败",e);
        }
        usrWorkTree = new TreeMap<>();
        untrackFile = new TreeMap<>();
        notInStageFile = new TreeMap<>();
        stageFile = new TreeMap<>();
    }

    @Override
    public void exec(String[] args) {
        if(args.length != 1){
            LOGGER.error("参数个数不匹配");
        }
        try {
            // 扫描用户空间下的文件
            searchUsrFile(new File(usrWorkspace));

            // 查找untrack file
            searchUnTrackFile();

            // 查找在缓存区但未提交的文件
            searchNotInStageFiles();

            // 查找准备提交的文件
            searchStageFile();

            printNotInStatgeFiles();
            printUntrackFiles();
            printStageFiles();
        } catch (SysException e){
            LOGGER.error("[StatusCommand] 失败",e);
        }

    }

    private void printStageFiles(){
        System.out.println("Changes staged for commit:");
        Set<String> paths = stageFile.keySet();
        for (String path : paths) {
            String type = stageFile.get(path);
            System.out.println("\t" + type + ":\t" + path);
        }
        System.out.println();
    }

    /**
     * 文件存在于lastcommit和index，但是两者的hash不同
     */
    private void searchStageFile() {
        Map<String, Entry> stageData = index.getData();
        Set<String> paths = stageData.keySet();
        for (String path : paths) {
            Entry stage = stageData.get(path);
            String stageHash = stage.getHash();
            if(notInStageFile.containsKey(path) || untrackFile.containsKey(path)){
                continue;
            }
            if(!usrWorkTree.containsKey(path)){
                stageFile.put(path, "deleted");
            }
            else if(lastCommitTree.containsKey(path)){
                Entry commitEntry = lastCommitTree.get(path);
                String commitHash = commitEntry.getHash();
                if(!commitHash.equals(stageHash)){
                    stageFile.put(path, "modified");
                }
            } else {
                stageFile.put(path, "new file");
            }
        }
    }

    private void printNotInStatgeFiles(){
        System.out.println("Changes not staged for commit:");
        Set<String> paths = notInStageFile.keySet();
        for (String path : paths) {
            Entry entry = notInStageFile.get(path);
            String type = "\tmodified:";
            if(entry.getDeleted()){
                type = "\tdeleted:";
            }
            System.out.println(type + "\t" + path);
        }
        System.out.println();
    }

    private void searchNotInStageFiles(){
        Map<String, Entry> stageData = index.getData();
        Set<String> stageFilePath = stageData.keySet();
        for (String path : stageFilePath) {
            Entry stageEntry = stageData.get(path);
            String stageHash = stageEntry.getHash();
            if(usrWorkTree.containsKey(path)){
                Entry usrEntry = usrWorkTree.get(path);
                String usrHash = usrEntry.getHash();
                if(!usrHash.equals(stageHash)){
                    notInStageFile.put(path, usrEntry);
                }
            } else if(!stageEntry.getDeleted()){
                // 被删除的文件，并且该文件的删除状态不在index中
               stageEntry.setDeleted(true);
               notInStageFile.put(path, stageEntry);
            }
        }
    }

    private void printUntrackFiles(){
        System.out.println("Untracked files:");
        Set<String> paths = untrackFile.keySet();
        for (String path : paths) {
            System.out.println("\t" + path);
        }
        System.out.println();
    }

    private void searchUnTrackFile(){
        Set<String> paths = usrWorkTree.keySet();
        for (String path : paths) {
            if(!index.containsFile(path) && !lastCommitTree.containsKey(path)){
                untrackFile.put(path, new Entry("", path, false));
            }
        }
    }

    private void searchUsrFile(File file) throws SysException {
        if(!file.isDirectory()){
            throw new SysException("[searchFile] 入参必须为目录");
        }
        File[] files = file.listFiles();

        // 查找存在的文件
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if(FileUtils.filter(name)){
                continue;
            }
            if(files[i].isDirectory()){
                searchUsrFile(files[i]);
            } else {
                String path = generateRelativePath(files[i]);
                Blob blob = new Blob(files[i],null);
                usrWorkTree.put(path, new Entry(blob.getContentHash(), path,false));
            }
        }
    }

    private String generateRelativePath(File file){
        File baseFile = new File(usrWorkspace);
        Path basePath = baseFile.toPath();
        Path path = file.toPath();
        Path relativize = basePath.relativize(path);

        return relativize.toString();
    }
}
