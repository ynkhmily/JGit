package com.zyh.Store;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

@Data
@AllArgsConstructor
public class Entry {

    private String hash;

    private String filePath;

    private Boolean deleted = false;

    public Entry(String filePath){
        this.filePath = filePath;
    }

    public void write(RandomAccessFile file) throws IOException {
        byte[] bytesHash = hash.getBytes(StandardCharsets.UTF_8);
        int lenHash = bytesHash.length;
        file.writeInt(lenHash);
        file.write(bytesHash);

        byte[] filePathBytes = filePath.getBytes(StandardCharsets.UTF_8);
        int filePathLen = filePathBytes.length;
        file.writeInt(filePathLen);
        file.write(filePathBytes);

        if(deleted){
            file.writeInt(1);
        } else {
            file.writeInt(0);
        }
    }

    public static Entry read(RandomAccessFile file) throws IOException {
        int lenHash = file.readInt();
        byte[] bytesHash = new byte[lenHash];

        file.read(bytesHash);
        String hash = new String(bytesHash,StandardCharsets.UTF_8);

        int filePathLen = file.readInt();
        byte[] filePathBytes = new byte[filePathLen];

        file.read(filePathBytes);
        String filePath = new String(filePathBytes,StandardCharsets.UTF_8);

        int deleted = file.readInt();
        if(deleted == 0){
            return new Entry(hash,filePath,false);
        } else{
            return new Entry(hash,filePath,true);
        }
    }

    @Override
    public String toString(){
        return hash + "\t" + filePath + "\t" + deleted;
    }
}
