package com.zyh.Store;

import com.zyh.Constant.SysConstant;
import com.zyh.Exception.SysException;
import com.zyh.Utils.SHA1Utils;
import lombok.Data;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;


@Data
public abstract class StoreFile {

    public final StoreType type;

    public StoreFile(StoreType type){
        this.type = type;
    }


    public abstract void writeToFile(String filePath) throws SysException;

    public abstract void catFile() throws SysException;

    public String compress(InputStream inputStream) throws SysException {
        try(DigestInputStream digestInputStream = new DigestInputStream(inputStream, MessageDigest.getInstance(SysConstant.HASH))) {
            while (digestInputStream.read() != -1) {
                // do nothing, just update digest
            }

            byte[] data = digestInputStream.getMessageDigest().digest();

            String hash = SHA1Utils.bytesToHex(data);

            return hash;
        } catch (Exception e){
            throw new SysException("[ZLibCompress] 失败",e);
        }
    }
}
