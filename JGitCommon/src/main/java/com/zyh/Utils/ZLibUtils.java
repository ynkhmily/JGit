package com.zyh.Utils;

import com.zyh.Exception.SysException;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import static com.zyh.Constant.SysConstant.BUFFER_SIZE;
import static com.zyh.Constant.SysConstant.FILE_MAX_SIZE;

public class ZLibUtils {

    public static byte[] compress(byte[] data,int size) throws SysException {
        Deflater deflater = new Deflater();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream, deflater)){

            deflaterOutputStream.write(data,0, size);
            deflaterOutputStream.finish();
            byte[] compressData = outputStream.toByteArray();

            return compressData;
        } catch (IOException e) {
            throw new SysException("[ZLibUtils][compress] 失败",e);
        }
    }

    public static String decompressData(RandomAccessFile file) throws SysException {
        try{
            long len = file.length() - file.getFilePointer();
            byte[] sourceData = new byte[(int) len];
            file.readFully(sourceData);

            Inflater inflater = new Inflater();
            InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(sourceData), inflater);

            // 创建一个足够大的缓冲区用于存放解压缩后的数据
            byte[] decompressedData = new byte[FILE_MAX_SIZE]; // 假设最大解压后大小为1MB，根据实际需求调整
            ByteArrayOutputStream baos = new ByteArrayOutputStream(decompressedData.length);

            // 解压缩并读取数据
            int decompressedLength;
            while ((decompressedLength = inflaterInputStream.read(decompressedData)) > 0) {
                baos.write(decompressedData, 0, decompressedLength);
            }

            // 关闭InflaterInputStream以确保数据完全解压
            inflaterInputStream.close();

            // 获取解压后的原始数据
            byte[] result = baos.toByteArray();

            return new String(result);
        } catch (IOException e) {
            throw new SysException("[ZLibUtils][decompressData] 失败",e);
        }
    }
}
