package com.rambo.httplib.utils;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class IOUtils {
    public IOUtils() {
    }

    public static File getSaveFolder(String folderName) {
        File file = new File(getSDCardPath() + File.separator + folderName + File.separator);
        file.mkdirs();
        return file;
    }

    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static byte[] input2byte(InputStream inStream) {
        if(inStream == null) {
            return null;
        } else {
            byte[] in2b = null;
            BufferedInputStream in = new BufferedInputStream(inStream);
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            boolean rc = false;

            try {
                int rc1;
                while((rc1 = in.read()) != -1) {
                    swapStream.write(rc1);
                }

                in2b = swapStream.toByteArray();
            } catch (IOException var9) {
                var9.printStackTrace();
            } finally {
                closeIO(new Closeable[]{inStream, in, swapStream});
            }

            return in2b;
        }
    }

    public static void closeIO(Closeable... closeables) {
        if(null != closeables && closeables.length > 0) {
            Closeable[] var1 = closeables;
            int var2 = closeables.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                Closeable cb = var1[var3];

                try {
                    if(null != cb) {
                        cb.close();
                    }
                } catch (IOException var6) {
                    throw new RuntimeException(IOUtils.class.getClass().getName(), var6);
                }
            }

        }
    }
}
