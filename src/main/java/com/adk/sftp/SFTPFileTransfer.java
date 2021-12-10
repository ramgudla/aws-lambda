package com.adk.sftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;

import com.jcraft.jsch.*;

public class SFTPFileTransfer {

    //private static final String REMOTE_HOST = "130.35.17.133";
    //private static final String USERNAME = "ramakrishna.rao.gudla@oracle.com";
    // private static final String PASSWORD = "Navadeep@14Rithvik@28";
	//private static final int REMOTE_PORT = 5032;
	
    private static final String REMOTE_HOST = "69.84.186.61";
    private static final String USERNAME = "BhAiInNeKeTEST";
    private static final String PASSWORD = "-[C+0kR5d1k";
    private static final int REMOTE_PORT = 22;
    
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 5000;

    public static void main(String[] args) {

        String localDir = "/Users/ramgudla/Desktop/Bugs/sftptest/";
        //String remoteDir = "/home/users/ramakrishna.rao.gudla@oracle.com";
        String remoteDir = "/BridgerRefactor-Staging/";

        Session jschSession = null;

        try {

            JSch jsch = new JSch();
            //ssh-keyscan -t rsa 69.84.186.61 >> ~/.ssh/known_hosts
            jsch.setKnownHosts("~/.ssh/known_hosts");
            
            jschSession = jsch.getSession(USERNAME, REMOTE_HOST, REMOTE_PORT);
            
            java.util.Properties config = new java.util.Properties(); 
            config.put("StrictHostKeyChecking", "no");
            jschSession.setConfig(config);

            // authenticate using private key
            // jsch.addIdentity("/Users/ramgudla/Desktop/Bugs/sftptest/id_rsa");

            // authenticate using password
            jschSession.setPassword(PASSWORD);

            // 10 seconds session timeout
            jschSession.connect(SESSION_TIMEOUT);

            Channel sftp = jschSession.openChannel("sftp");

            // 5 seconds timeout
            sftp.connect(CHANNEL_TIMEOUT);

            ChannelSftp channelSftp = (ChannelSftp) sftp;
            //channelSftp.cd(".");

            File folder = new File(localDir);
            
            FilenameFilter txtFileFilter = new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    if(name.endsWith(".xml"))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            };

            //Passing txtFileFilter to listFiles() method to retrieve only txt files

            File[] files = folder.listFiles(txtFileFilter);

            for (File file : files)
            {
            	System.out.println("Putting file: " + file.getName());
            	channelSftp.put(file.getAbsolutePath(), remoteDir + file.getName());
            }
            
			// download file from remote server to local
			channelSftp.get(remoteDir + "good.txt", localDir + "download.txt");

            channelSftp.exit();

        } catch (JSchException | SftpException e) {
            e.printStackTrace();

        } finally {
            if (jschSession != null) {
                jschSession.disconnect();
            }
        }

        System.out.println("Done");
    }
    
    private static void touch(File file) throws IOException{
        long timestamp = System.currentTimeMillis();
        touch(file, timestamp);
    }

    private static void touch(File file, long timestamp) throws IOException{
        if (!file.exists()) {
           new FileOutputStream(file).close();
        }

        file.setLastModified(timestamp);
    }


}