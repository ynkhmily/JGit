package com.zyh;

import com.zyh.Command.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class JGitClient {

    public static final Logger LOGGER = LoggerFactory.getLogger(JGitClient.class);

    public final String workspace;

    public JGitClient(String workspace) {
        this.workspace = workspace;
    }


    public static void main(String[] args) {
//        TODO 待修改
//        String workspace = System.getProperty("user.dir");
//        String workspace = "E:\\zyh\\java\\JGit-Test";
        String workspace = "E:\\zyh\\java\\新建文件夹";
//        JGitClient jGitClient = new JGitClient(workspace);
        try {
            if (args.length > 0) {
                String command = args[0];
                if(command.equals("init")){
                    Context context = new Context(new InitCommand(workspace));
                    context.exec(args);
                } else if(command.equals("add")){
                    Context context = new Context(new AddCommand(workspace));
                    context.exec(args);
                } else if(command.equals("cat-file")){
                    Context context = new Context(new CatFileCommand(workspace));
                    context.exec(args);
                } else if(command.equals("ls-files")){
                    Context context = new Context(new LsFileCommand(workspace));
                    context.exec(args);
                } else if(command.equals("commit")){
                    Context context = new Context(new CommitCommand(workspace));
                    context.exec(args);
                } else if(command.equals("status")){
                    Context context = new Context(new StatusCommand(workspace));
                    context.exec(args);
                }

            } else {
                System.out.println("--help");
            }
        } catch (Exception e){
            LOGGER.error(e.toString());
        }
    }

}


