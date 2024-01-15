package fi.methics.webapp.musaplink.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class CouplingCode {

    public static final String BASE64_URL_MARKER = "data:image/png;base64,";
    public static final String CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final SecureRandom RANDOM = new SecureRandom();
    
    public String code;
    
    /**
     * Parse an CouplingCode from String
     * @param couplingCode CouplingCode
     */
    public CouplingCode(String couplingCode) {
        this.code = couplingCode;
    }
    
    /**
     * Generate a new Coupling Code
     */
    public CouplingCode() {
        this.code = getRandomString(6);
    }
    
    /**
     * Get the coupling code as String
     * @return coupling code
     */
    public String getCode() {
        return this.code;
    }
    
    /**
     * Create a QR code out of this CouplingCode
     * @return QR image bytes
     * @throws IOException     if QR writing fails for any ImageIO reason
     * @throws WriterException if QR writing fails for any ZXing related reason
     */
    public byte[] toQRImage() throws IOException, WriterException 
    {
        Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hintMap.put(EncodeHintType.MARGIN, Integer.valueOf(1));
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix    matrix = writer.encode(this.getCode(), BarcodeFormat.QR_CODE, 250, 250, hintMap);
        int          width  = matrix.getWidth();
        int          height = matrix.getHeight();

        BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_BYTE_BINARY);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                if (matrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", os);
            os.flush();
            return os.toByteArray();
        }
    }
    
    /**
     * Convert this to a QR URL (data:image/png;base64,...)
     * @return QR URL or null if QR cannot be created
     */
    public String toURL() {
        try {
            return BASE64_URL_MARKER + Base64.getEncoder().encodeToString(this.toQRImage());
        } catch (Exception e) {
            return null;
        }
    }
    

    private static String getRandomString(int len){
       StringBuilder sb = new StringBuilder(len);
       for(int i = 0; i < len; i++) {
          sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
       }
       return sb.toString();
    }
    
}
