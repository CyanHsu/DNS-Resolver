import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Arrays;

public class DNSServer {
    DatagramSocket datagramSocket_;
    public DNSServer(int port) throws IOException {


        // create a server
        datagramSocket_ = new DatagramSocket(port);
        System.out.println("Waiting for request");

        DNSCache cache = new DNSCache();


        byte[] receive = new byte[512];
        DatagramPacket dpReceiveFromUser = null;



        while(true){

            // receive data from user, check the table in cache
            dpReceiveFromUser = new DatagramPacket(receive, receive.length);
            datagramSocket_.receive(dpReceiveFromUser);
            System.out.println("received something from User");

            long start = System.currentTimeMillis();

            DNSMessage dnsMessageReceiveFromUser = DNSMessage.decodeMessage(dpReceiveFromUser.getData());


            // if there is no answer in cache, send the request to google
            if(!cache.contains(dnsMessageReceiveFromUser)){
                InetAddress ip = InetAddress.getByName("8.8.8.8");//google
                DatagramPacket sendToGoogle = new DatagramPacket(receive, receive.length, ip, 53);
                datagramSocket_.send(sendToGoogle);
                System.out.println("send to Google");

                // Receive answer from Google
                byte[] receiveFromGoogle = new byte[512];
                DatagramPacket dpReceiveFromGoogle = new DatagramPacket(receiveFromGoogle, receiveFromGoogle.length);
                datagramSocket_.receive(dpReceiveFromGoogle);
                System.out.println("received something from User Google");

                // parse the response from google, save the answer into cache, and send response back to user
                DNSMessage dnsMessageReceiveFromGoogle = DNSMessage.decodeMessage(dpReceiveFromGoogle.getData());
                System.out.println("ancount from google: " + dnsMessageReceiveFromGoogle.dnsHeader_.ancount_);

                if(dnsMessageReceiveFromGoogle.dnsHeader_.ancount_ > 0){ // check if it's a valid domain name with ip
                    //add question into cache
                    cache.add(dnsMessageReceiveFromGoogle);
                }
                else {
                    System.err.println("No such domain name.");
                }
                //Send to User
                DatagramPacket sendToUser = new DatagramPacket(receiveFromGoogle, receiveFromGoogle.length, dpReceiveFromUser.getAddress(), dpReceiveFromUser.getPort());

                datagramSocket_.send(sendToUser);
                System.out.println("send to User" + "ip : " + dpReceiveFromUser.getAddress() + "port:" + dpReceiveFromUser.getPort());
            }
            else {  // if there is answer in cache, generate the response data and send back to user
                System.out.println("Contain answer in cache");
                DNSMessage response = DNSMessage.buildResponse(dnsMessageReceiveFromUser,cache.getAnswer(dnsMessageReceiveFromUser));

                DatagramPacket sendToUser = new DatagramPacket(response.toBytes(),response.toBytes().length, dpReceiveFromUser.getAddress(),dpReceiveFromUser.getPort());
                datagramSocket_.send(sendToUser);
                System.out.println("send to User" + "ip : " + dpReceiveFromUser.getAddress() + "port:" + dpReceiveFromUser.getPort());
            }
            long end = System.currentTimeMillis();
            long total = end -start;
            System.out.println("It takes " + total + "millis");
        }
    }
}
