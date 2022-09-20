package com.lollipop.palette;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ColorHistoryWriter {

    public static void write(List<Integer> colorList, OutputStream outputStream) throws IOException {
        List<Integer> list = new ArrayList<>(colorList);
        byte[] byteBuffer = new byte[4];
        for (Integer color : list) {
            byteBuffer[0] = (byte)(color >>> 24 & 0xFF);
            byteBuffer[1] = (byte)(color >>> 16 & 0xFF);
            byteBuffer[2] = (byte)(color >>> 8 & 0xFF);
            byteBuffer[3] = (byte)(color & 0xFF);
            outputStream.write(byteBuffer);
        }
        outputStream.flush();
    }

    public static List<Integer> read(InputStream inputStream) throws IOException {
        ArrayList<Integer> list = new ArrayList<>();
        byte[] byteBuffer = new byte[4];
        int lengthMax = byteBuffer.length - 1;
        do {
            int length = inputStream.read(byteBuffer);
            if (length < 0) {
                break;
            }
            if (length < lengthMax) {
                continue;
            }
            list.add((byteBuffer[0] << 24 & 0xFF000000) | (byteBuffer[1] << 16 & 0xFF0000) | (byteBuffer[2] << 8 & 0xFF00) | (byteBuffer[3] & 0xFF));
        } while (true);
        return list;
    }

}
