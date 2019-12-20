package com.zylitics.btbr.util;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtil {
  
  /**
   * use this method to get an {@link ByteArrayInputStream} from the
   * {@link RenderedImage}
   * @param bufImg a {@link RenderedImage}
   * @param imgFormat example 'png'
   * @return Instance of an {@link ByteArrayInputStream}
   * @throws IOException If there are problems processing the image
   */
  static InputStream toByteArrayInputStream(RenderedImage bufImg
      , String imgFormat) throws IOException {
    byte[] bytes = getBytesFromImage(bufImg, imgFormat);
    return new ByteArrayInputStream(bytes);
  }
  
  private static byte[] getBytesFromImage(RenderedImage bufImg, String imgFormat)
      throws IOException {
    ByteArrayOutputStream bOStream = new ByteArrayOutputStream(2000000);    // buffer size 2MB
    ImageIO.setUseCache(false);    // disallow disk access while writing to output stream
    ImageIO.write(bufImg, imgFormat, bOStream);    // closing not required
    return bOStream.toByteArray();
  }
}