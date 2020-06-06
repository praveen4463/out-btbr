package com.zylitics.btbr.shot;

import org.apache.commons.lang3.NotImplementedException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class JavaRobotCaptureDevice implements CaptureDevice {
  
  private final String shotExtension;
  
  private JavaRobotCaptureDevice(String shotExtension) {
    this.shotExtension = shotExtension;
  }
  
  /*
  @Override
  public Result captureFull()
      throws Exception {
    long startTime = System.nanoTime();
    Rectangle size = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    System.out.println("It took rectangle build {} millis"
        , TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
    startTime = System.nanoTime();
    BufferedImage shot = new Robot().createScreenCapture(size);
    System.out.println("It took screen capture {} millis"
        , TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
    startTime = System.nanoTime();
    Entry<Integer, InputStream> iStream = ImageUtil.toByteArrayInputStream(shot
        , S3ShotMetadata.SHOT_EXT);
    System.out.println("It took {} millis to convert shot to stream"
        , TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
    return new Result(iStream);
  }*/
  
  @Override
  public void init() {
    // just run these to load the classes in VM. There is no other significance of capturing screen
    // from here.
    Rectangle size = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    try {
      new Robot().createScreenCapture(size);
    } catch (AWTException awt) {
      // ignore
    }
  }
  
  @Override
  public Result captureFull() throws Exception {
    /*
     * Performance notes:
     * getScreenSize and createScreenCapture methods have been optimized to function in acceptable
     * times even if its the first time access in the JVM.
     * image to stream method is also optimized to reserve a large enough buffer before writing
     * image bytes. It also doesn't use disk for caching.
     */
    Rectangle size = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    BufferedImage shot = new Robot().createScreenCapture(size);
    
    InputStream iStream = toByteArrayInputStream(shot, shotExtension);
    return new Result(iStream);
  }
  
  @Override
  public Result captureVisible() {
    /*
     * Future notes: BufferedImage.getSubimage(x, y, w, h) can be used to get sub images from main
     * image and also can calculate the screen size to get the only visible part and not the
     * CHROME of the OS or browser.
     */
    throw new NotImplementedException("");
  }
  
  /**
   * use this method to get an {@link ByteArrayInputStream} from the
   * {@link RenderedImage}
   * @param bufImg a {@link RenderedImage}
   * @param imgFormat example 'png'
   * @return Instance of an {@link ByteArrayInputStream}
   * @throws IOException If there are problems processing the image
   */
   private InputStream toByteArrayInputStream(RenderedImage bufImg
      , String imgFormat) throws IOException {
    byte[] bytes = getBytesFromImage(bufImg, imgFormat);
    return new ByteArrayInputStream(bytes);
  }
  
  // !! Note that it turns out Java 8 already compresses png to maximum possible
  // https://stackoverflow.com/a/54422581/1624454
  // We may not use jpg for more compression cause they're best for pictures not web pages.
  private static byte[] getBytesFromImage(RenderedImage bufImg, String imgFormat)
      throws IOException {
    ByteArrayOutputStream bOStream = new ByteArrayOutputStream(2000000);    // buffer size 2MB
    ImageIO.setUseCache(false);    // disallow disk access while writing to output stream
    ImageIO.write(bufImg, imgFormat, bOStream);    // closing not required
    return bOStream.toByteArray();
  }
  
  private static class Result implements CaptureDevice.Result {
    
    private final InputStream shotInputStream;
    
    Result(InputStream shotInputStream) {
      this.shotInputStream = shotInputStream;
    }
    
    @Override
    public InputStream getShotInputStream() {
      return shotInputStream;
    }
  }
  
  static class Factory implements CaptureDevice.Factory {
    
    @Override
    public CaptureDevice create(String shotExtension) {
      return new JavaRobotCaptureDevice(shotExtension);
    }
  }
}
