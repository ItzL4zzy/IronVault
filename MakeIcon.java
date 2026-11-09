import com.ironvault.ui.Icons;
import com.ironvault.ui.Theme;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Writes a Windows .ico file with uncompressed BMP frames (16..256 px). */
public class MakeIcon {
    private static final int[] SIZES = {16, 24, 32, 48, 64, 128, 256};

    public static void main(String[] args) throws Exception {
        Path out = Path.of(args.length > 0 ? args[0] : "dist/IronVault.ico");
        int[] sizes = SIZES;
        if (args.length > 1) {
            String[] parts = args[1].split(",");
            sizes = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                sizes[i] = Integer.parseInt(parts[i].trim());
            }
        }
        Theme.install();
        List<byte[]> frames = new ArrayList<>();
        for (int size : sizes) {
            frames.add(bmpFrame(Icons.appIcon(size), size));
        }
        Path parent = out.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        try (DataOutputStream os = new DataOutputStream(Files.newOutputStream(out))) {
            writeShortLE(os, 0);              // reserved
            writeShortLE(os, 1);              // type: icon
            writeShortLE(os, sizes.length);   // frame count
            int offset = 6 + 16 * sizes.length;
            for (int i = 0; i < sizes.length; i++) {
                int size = sizes[i];
                byte[] frame = frames.get(i);
                os.writeByte(size >= 256 ? 0 : size);
                os.writeByte(size >= 256 ? 0 : size);
                os.writeByte(0);              // palette size
                os.writeByte(0);              // reserved
                writeShortLE(os, 1);          // color planes
                writeShortLE(os, 32);         // bits per pixel
                writeIntLE(os, frame.length);
                writeIntLE(os, offset);
                offset += frame.length;
            }
            for (byte[] frame : frames) {
                os.write(frame);
            }
        }
        System.out.println("Icon written: " + out.toAbsolutePath());
    }

    private static byte[] bmpFrame(BufferedImage image, int size) throws IOException {
        int rowSize = size * 4;
        int maskRowSize = ((size + 31) / 32) * 4;
        int maskSize = maskRowSize * size;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream os = new DataOutputStream(bytes)) {
            writeIntLE(os, 40);               // BITMAPINFOHEADER
            writeIntLE(os, size);             // width
            writeIntLE(os, size * 2);         // height: XOR + AND mask
            writeShortLE(os, 1);              // planes
            writeShortLE(os, 32);             // bpp
            writeIntLE(os, 0);                // BI_RGB
            writeIntLE(os, rowSize * size + maskSize);
            writeIntLE(os, 0);
            writeIntLE(os, 0);
            writeIntLE(os, 0);
            writeIntLE(os, 0);
            for (int y = size - 1; y >= 0; y--) {
                for (int x = 0; x < size; x++) {
                    int argb = image.getRGB(x, y);
                    os.writeByte(argb & 0xFF);           // blue
                    os.writeByte((argb >> 8) & 0xFF);    // green
                    os.writeByte((argb >> 16) & 0xFF);   // red
                    os.writeByte((argb >>> 24) & 0xFF);  // alpha
                }
            }
            byte[] emptyMaskRow = new byte[maskRowSize];
            for (int y = 0; y < size; y++) {
                os.write(emptyMaskRow);
            }
        }
        return bytes.toByteArray();
    }

    private static void writeShortLE(DataOutputStream os, int value) throws IOException {
        os.writeByte(value & 0xFF);
        os.writeByte((value >> 8) & 0xFF);
    }

    private static void writeIntLE(DataOutputStream os, int value) throws IOException {
        os.writeByte(value & 0xFF);
        os.writeByte((value >> 8) & 0xFF);
        os.writeByte((value >> 16) & 0xFF);
        os.writeByte((value >> 24) & 0xFF);
    }
}
