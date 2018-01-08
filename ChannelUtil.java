package cn.com.cmt.testany.channel;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Created by chaoranf on 2018/1/8.
 */

public class ChannelUtil {

    public static String getChannel(Context context) {
        String packagePath = context.getPackageCodePath();
        String channel = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                String fileDir = context.getApplicationInfo().sourceDir;
                ZipFile file = new ZipFile(fileDir);
                channel = file.getComment();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return channel;
        }

        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(packagePath, "r");
            channel = readChannel(raf);
        } catch (IOException e) {
            // ignore
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return channel;
    }

    private static final long LOCSIG = 0x4034b50;
    private static final long ENDSIG = 0x6054b50;
    private static final int ENDHDR = 22;

    private static short peekShort(byte[] src, int offset) {
        return (short) ((src[offset + 1] << 8) | (src[offset] & 0xff));
    }

    private static String readChannel(RandomAccessFile raf) throws IOException {
        // Scan back, looking for the End Of Central Directory field. If the zip file doesn't
        // have an overall comment (unrelated to any per-entry comments), we'll hit the EOCD
        // on the first try.
        // No need to synchronize raf here -- we only do this when we first open the zip file.
        long scanOffset = raf.length() - ENDHDR;
        if (scanOffset < 0) {
            throw new ZipException("File too short to be a zip file: " + raf.length());
        }

        raf.seek(0);
        final int headerMagic = Integer.reverseBytes(raf.readInt());
        if (headerMagic == ENDSIG) {
            throw new ZipException("Empty zip archive not supported");
        }
        if (headerMagic != LOCSIG) {
            throw new ZipException("Not a zip archive");
        }

        long stopOffset = scanOffset - 65536;
        if (stopOffset < 0) {
            stopOffset = 0;
        }

        while (true) {
            raf.seek(scanOffset);
            if (Integer.reverseBytes(raf.readInt()) == ENDSIG) {
                break;
            }

            scanOffset--;
            if (scanOffset < stopOffset) {
                throw new ZipException("End Of Central Directory signature not found");
            }
        }

        // Read the End Of Central Directory. ENDHDR includes the signature bytes,
        // which we've already read.
        byte[] eocd = new byte[ENDHDR - 4];
        raf.readFully(eocd);

        // Pull out the information we need.
        int position = 0;
        int diskNumber = peekShort(eocd, position) & 0xffff;
        position += 2;
        int diskWithCentralDir = peekShort(eocd, position) & 0xffff;
        position += 2;
        int numEntries = peekShort(eocd, position) & 0xffff;
        position += 2;
        int totalNumEntries = peekShort(eocd, position) & 0xffff;
        position += 2;
        position += 4; // Ignore centralDirSize.
        // long centralDirOffset = ((long) peekInt(eocd, position)) & 0xffffffffL;
        position += 4;
        int commentLength = peekShort(eocd, position) & 0xffff;
        position += 2;

        if (numEntries != totalNumEntries || diskNumber != 0 || diskWithCentralDir != 0) {
            throw new ZipException("Spanned archives not supported");
        }

        String comment = "";
        if (commentLength > 0) {
            byte[] commentBytes = new byte[commentLength];
            raf.readFully(commentBytes);
            comment = new String(commentBytes, 0, commentBytes.length, Charset.forName("UTF-8"));
        }
        return comment;
    }
}