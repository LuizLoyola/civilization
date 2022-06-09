package br.com.tiozinnub.civilization.utils.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamHelper {
    public static String readInputStream(InputStream inputStream) throws IOException {
        var result = new ByteArrayOutputStream();
        var buffer = new byte[1024];
        for (int length; (length = inputStream.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        return result.toString();
    }
}
