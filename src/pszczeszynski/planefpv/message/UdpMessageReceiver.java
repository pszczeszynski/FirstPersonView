package pszczeszynski.planefpv.message;

import pszczeszynski.planefpv.DisplayWindow;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import static pszczeszynski.planefpv.DisplayWindow.GetTimeMillis;

public class UdpMessageReceiver implements Runnable {
    final int port = 11116;

    private boolean stop = false;
    private DatagramSocket udpSocket;
    long lastReceiveTime = 0;

    public UdpMessageReceiver()
    {
        try {
            udpSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.out.println("pszczesz: error creating udpSocket: " + e.toString());
        }

    }


    long numFrames = 0;
    long timeOfFirstFrame = 0;
    @Override
    public void run() {
        try {
            while (!stop) {
                byte[] message = new byte[100000];
                DatagramPacket packet = new DatagramPacket(message, message.length);
//                System.out.println("pszczesz: UDP client: about to wait to receive");
                udpSocket.receive(packet);
                if (numFrames % 100 == 0)
                {
                    System.out.println("pszczesz: packets/sec: " + 1000.0 * numFrames / (GetTimeMillis()-timeOfFirstFrame));
                }

                DisplayWindow.instance.AddImage(message);

                if (GetTimeMillis() - lastReceiveTime > 45)
                {
                    System.out.println("long frame");
                }

//                if (GetTimeMillis() - lastReceiveTime < 15)
//                {
//                    System.out.println("short frame");
//                }
//                System.out.println("frame was null after method: " + (DisplayWindow.lastFrame == null));

                lastReceiveTime = GetTimeMillis();
                numFrames ++;
                if (timeOfFirstFrame == 0){
                    timeOfFirstFrame = GetTimeMillis();
                }
            }
            System.out.println("pszczesz: stopped receiving");
        }catch (Exception e) {
            System.out.println("pszczesz: UDP Exception error: " + e);
        }
    }


    public void StopReceiving() {
        stop = true;
        udpSocket.close();
    }
}