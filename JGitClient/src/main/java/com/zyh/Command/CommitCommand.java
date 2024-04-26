package com.zyh.Command;

import com.zyh.Constant.SysConstant;
import com.zyh.Exception.SysException;
import com.zyh.Store.*;
import com.zyh.Utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class CommitCommand implements Command{

    public static final Logger LOGGER = LoggerFactory.getLogger(CommitCommand.class);

    private String usrWorkspace;

    private String sysWorkspace;

    private Index index;

    private Commit lastCommit;

    public CommitCommand(String usrWorkspace){
        this.usrWorkspace = usrWorkspace;
        this.sysWorkspace = FileUtils.generateFilePath(usrWorkspace, SysConstant.WORK_ROOT_DIR);
        try {
            index = Index.generateIndexFile(sysWorkspace);
            File headFile = new File(FileUtils.generateFilePath(sysWorkspace, Commit.HEAD));
            lastCommit = (Commit) FileUtils.readFromStoreFile(headFile);
        } catch (SysException e) {
            LOGGER.error("[CommitCommand] 失败,",e);
        }
    }

    @Override
    public void exec(String[] args) {
        if(args.length != 2){
            LOGGER.error("[CommitCommand] 参数个数不匹配, 需要添加提交信息：msg");
        }
        try {
            String msg = args[1];
            List<String> parents = new ArrayList<>();
            if(!Objects.isNull(lastCommit)) {
                String lastCommitHash = lastCommit.getHash();
                parents.add(lastCommitHash);
            }

            Tree tree = buildTree();
            Entry child = new Entry(tree.getHash(), tree.getDir(), false);
            Commit commit = new Commit(msg, child, parents);

            // 设置为HEAD
            commit.setIsHead(true);
            commit.writeToFile(FileUtils.generateFilePath(sysWorkspace, SysConstant.WORK_OBJECTS_DIR));
        } catch (SysException e){
            LOGGER.error("[CommitCommand] 失败,",e);
        }
    }

    private Tree buildTree() throws SysException {
        Map<String, Entry> stageData = index.getData();
        Map<String, Tree> map = new TreeMap<>(new TreeComparator());

        Set<String> filePaths = stageData.keySet();
        String sysObjectWorkspace = FileUtils.generateFilePath(sysWorkspace, SysConstant.WORK_OBJECTS_DIR);
        List<Entry> topLevelBlobEntry = new ArrayList<>();
        for (String filePath : filePaths) {
            Entry entry = stageData.get(filePath);
            String hash = entry.getHash();

            // 读取blob文件
            File targetFile = FileUtils.findStoreFile(sysObjectWorkspace, hash);
            Blob blobFile = (Blob) FileUtils.readFromStoreFile(targetFile);

            // 加载Tree对象
            loadTree(filePath, blobFile, map);
            // 如果该文件位于顶层
            if(!filePath.contains(File.separator)){
                topLevelBlobEntry.add(entry);
            }
        }

        // 写入磁盘
        Tree tree = writeTree(map, topLevelBlobEntry);

        return tree;
    }

    /**
     * 读取目录的每一层
     * @param filePath
     * @return
     */
    private List<String> getFilePathList(String filePath){
        String[] dirs = filePath.split(Pattern.quote(File.separator));
        ArrayList<String> pathList = new ArrayList<>();
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < dirs.length; i++) {
            if(path.length() != 0){
                path.append(File.separator);
            }
            path.append(dirs[i]);
            pathList.add(path.toString());
        }

        return pathList;
    }

    /**
     * 生成目录的Tree对象，并放入map中
     * @param filePath
     * @param blobFile
     * @param map
     * @throws SysException
     */
    private void loadTree(String filePath, Blob blobFile, Map<String, Tree> map) throws SysException {
        // 加载文件夹tree对象
        List<String> filePathList = getFilePathList(filePath);
        if (filePathList.size() != 1) {
            // 从倒数第二个开始，倒数一个为blob文件
            Entry last = new Entry(blobFile.getHashByFileName(), filePath, false);
            for (int i = filePathList.size() - 2; i >= 0; i--) {
                String currentPath = filePathList.get(i);
                String[] dirs = currentPath.split(Pattern.quote(File.separator));
                String dir = dirs[dirs.length - 1];

                Tree currentTree;
                if (map.containsKey(currentPath)) {
                    currentTree = map.get(currentPath);
                } else {
                    currentTree = new Tree(dir);
                    map.put(currentPath, currentTree);
                }
                currentTree.addChild(last);
                last = new Entry(dir);
            }
        }
    }

    private Tree writeTree(Map<String, Tree> map, List<Entry> topLevelBlobEntry) throws SysException {
        // 生成Hash，完善Tree对象
        Set<String> pathSet = map.keySet();
        String filePath = FileUtils.generateFilePath(sysWorkspace, SysConstant.WORK_OBJECTS_DIR);
        for (String path : pathSet) {
            Tree tree = map.get(path);
            // Tree对象对应的Entry缺少hash
            tree.completeEntryHash(map, path);
            // 写入文件
            tree.writeToFile(filePath);
        }

        // 生成最上层的Tree对象，并写入
        Tree tree = new Tree(SysConstant.DEFAULT_DIR);
        for (String path : pathSet) {
            if(!path.contains(File.separator)){
                Tree childTree = map.get(path);
                tree.addChild(new Entry(childTree.getHash(), childTree.getDir(), false));
            }
        }
        for (Entry entry : topLevelBlobEntry) {
            tree.addChild(entry);
        }
        tree.writeToFile(FileUtils.generateFilePath(sysWorkspace, SysConstant.WORK_OBJECTS_DIR));

        return tree;
    }


    class TreeComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            Integer x = o1.split(Pattern.quote(File.separator)).length;
            Integer y = o2.split(Pattern.quote(File.separator)).length;

            return (x > y) ? -1 : ((x == y) ? 0 : 1);
        }
    }
}
