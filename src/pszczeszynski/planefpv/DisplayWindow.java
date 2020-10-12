package pszczeszynski.planefpv;

import pszczeszynski.planefpv.message.UdpMessageReceiver;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.Semaphore;

public class DisplayWindow {
    public static DisplayWindow instance;
    private JFrame frame;
    private UdpMessageReceiver messageReceiver;

    Semaphore mainThreadLock = new Semaphore(1);
    // the current image we are displaying
    private BufferedImage displayImage;

    public static volatile byte[] lastFrame = null;
    /**
     * Displays an image to the screen
     * @param bytes the bytes of the image
     */
    public void AddImage(byte[] bytes) throws InterruptedException, IOException {
        mainThreadLock.acquire();
        lastFrame = bytes;
        ByteArrayInputStream bis = new ByteArrayInputStream(lastFrame);
        displayImage = ImageIO.read(bis);

        Graphics g = frame.getGraphics();
        g.drawImage(displayImage, 0, 0,frame.getWidth(),frame.getWidth() * 9/16, frame);
        lastFrame = null;
        mainThreadLock.release();

        mainThreadLock.release();
    }

    public static long GetTimeMillis()
    {
        return Calendar.getInstance().getTimeInMillis();
    }


    long firstDisplayTime = 0;
    long numFrames = 0;
    public void DisplayFrame() throws IOException, InterruptedException {
        while (lastFrame == null) {
            Thread.onSpinWait();
        }

        mainThreadLock.acquire();

        if (firstDisplayTime == 0)
        {
            firstDisplayTime = GetTimeMillis();
        }
        numFrames ++;
        if(numFrames % 50 == 0)
        {
            System.out.println("fps displaying: " + (1000.0 * numFrames / (GetTimeMillis() - firstDisplayTime)));
        }



        ByteArrayInputStream bis = new ByteArrayInputStream(lastFrame);
        displayImage = ImageIO.read(bis);
        Image image = displayImage.getScaledInstance(1920,1080,Image.SCALE_FAST);
        Graphics g = frame.getGraphics();
        g.drawImage(displayImage, 0, 0, frame);
        lastFrame = null;
        mainThreadLock.release();
    }

    public static void main(String[] args) {
        instance = new DisplayWindow();
        instance.SetupFrame();
        instance.messageReceiver = new UdpMessageReceiver();
        // start receiving messages
        new Thread(instance.messageReceiver).start();

//        while (true) {
//            try {
//                instance.DisplayFrame();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private void SetupFrame() {
        frame = new JFrame();
        frame.setSize(new Dimension(1920, 1080));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        frame.pack();
        frame.setVisible(true);
    }
}
