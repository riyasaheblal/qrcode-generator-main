package de.ksbrwsk.qrcode.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import de.ksbrwsk.qrcode.model.*;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import static de.ksbrwsk.qrcode.util.Constants.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class QrCodeEncoder {

    private final ResourceLoader resourceLoader;

    public QrCodeEncoder(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public QrCodeProcessingResult generateQrCodeUrl(QrCodeUrl qrCodeUrl) {
        String extracted = new QrCodeUrlParser(qrCodeUrl).parse();
        return this.generateImageAsBase64(extracted, null);
    }

    public QrCodeProcessingResult generateQrCodeEmail(QrCodeEmail qrCodeEmail) {
        String extracted = new QrCodeEmailParser(qrCodeEmail).parse();
        return this.generateImageAsBase64(extracted,null);
    }

    public QrCodeProcessingResult generateQrCodeSms(QrCodeSms qrCodeSms) {
        String extracted = new QrCodeSmsParser(qrCodeSms).parse();
        return this.generateImageAsBase64(extracted,null);
    }

    public QrCodeProcessingResult generateQrCodePhone(QrCodePhone qrCodePhone) {
        String extracted = new QrCodePhoneParser(qrCodePhone).parse();
        return this.generateImageAsBase64(extracted,null);
    }

    public QrCodeProcessingResult generateQrCodeEvent(QrCodeEvent qrCodeEvent) {
        String extracted = new QrCodeEventParser(qrCodeEvent).parse();
        return this.generateImageAsBase64(extracted,null);
    }

    public QrCodeProcessingResult generateQrCodeFacetime(QrCodeFacetime qrCodeFacetime) {
        String extracted = new QrCodeFacetimeParser(qrCodeFacetime).parse();
        return this.generateImageAsBase64(extracted,null);
    }

    public QrCodeProcessingResult generateQrCodeVCard(QrCodeVCard qrCodeVCard) {
        String extracted = new QrCodeVCardParser(qrCodeVCard).parse();
        return this.generateImageAsBase64(extracted,qrCodeVCard.getLogo());
    }

    private QrCodeProcessingResult generateImageAsBase64(String textToBeEncoded, MultipartFile file) {
        QrCodeProcessingResult result = new QrCodeProcessingResult();
        result.setEncodedText(textToBeEncoded);
        String imageText = "";
        //String imgFileType = Img_File_Type;
        int size = QR_CODE_SIZE;
        String fileType = PNG;
        BufferedImage logoImage;
        try {
            Map<EncodeHintType, Object> hintMap = createHintMap();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(textToBeEncoded, BarcodeFormat.QR_CODE, size, size, hintMap);

            /*Resource resource = resourceLoader.getResource("classpath:/static/image/" + imgFileType);
            try (InputStream inputStream = resource.getInputStream()) {
                logoImage =  ImageIO.read(inputStream);
            }*/


            //BufferedImage logoImage = ImageIO.read(new File(logoPath));

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            image.createGraphics();
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    if (bitMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }

            if (file.getName().toString().trim() != null){
                logoImage = ImageIO.read (file.getInputStream());
                int logoWidth = Logo_Size;
                int logoHeight = Logo_Size;
                int xPos = (width - logoWidth) / 2;
                int yPos = (height - logoHeight) / 2;
                graphics.drawImage(logoImage, xPos, yPos, logoWidth, logoHeight, null);
                graphics.dispose();
            }

            String fileName = UUID.randomUUID().toString();
            File myFile = File.createTempFile(fileName, "." + fileType);
            ImageIO.write(image, fileType, myFile);
            byte[] bytes = FileUtils.readFileToByteArray(myFile);
            imageText = "data:image/png;base64," +
                    Base64.getEncoder().encodeToString(bytes);
            result.setImage(imageText);
        } catch (WriterException | IOException e) {
            String msg = "Processing QR code failed.";
            log.error(msg, e);
            result.setErrorMessage(msg);
        }
        log.info("QR Code for text {} was successfully created.", textToBeEncoded);
        result.setSuccessMessage("QR Code was successfully created.");
        return result;
    }

    @NotNull
    private Map<EncodeHintType, Object> createHintMap() {
        Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
        hintMap.put(EncodeHintType.CHARACTER_SET, UTF_TYPE);
        hintMap.put(EncodeHintType.MARGIN, 1);
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        return hintMap;
    }

}
