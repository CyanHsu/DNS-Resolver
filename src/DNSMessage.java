import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DNSMessage {
    DNSHeader dnsHeader_;

    ArrayList<DNSQuestion> questions_ = new ArrayList<>();
    ArrayList<DNSRecord> answers_ = new ArrayList<>();
    ArrayList<DNSRecord> authorities_ = new ArrayList<>();
    ArrayList<DNSRecord> additional_ = new ArrayList<>();
    boolean hasAnswer_;


    byte[] completeMessage_;




    static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        DNSMessage dnsMessage = new DNSMessage();
        dnsMessage.completeMessage_ = bytes;
        InputStream inputStream = new ByteArrayInputStream(bytes);


        dnsMessage.dnsHeader_ = DNSHeader.decodeHeader(inputStream);

        dnsMessage.hasAnswer_ = dnsMessage.dnsHeader_.ancount_ > 0;
        for(int i = 0; i < dnsMessage.dnsHeader_.qdcount_; i++) {
            dnsMessage.questions_.add(DNSQuestion.decodeQuestion(inputStream, dnsMessage));
        }
        for (int i = 0; i < dnsMessage.dnsHeader_.ancount_; i++) {
            dnsMessage.answers_.add(DNSRecord.decodeRecord(inputStream,dnsMessage));
        }
        dnsMessage.hasAnswer_ = false;
        for (int i = 0; i < dnsMessage.dnsHeader_.nscount_; i++) {
            dnsMessage.authorities_.add(DNSRecord.decodeRecord(inputStream,dnsMessage));
        }
        for (int i = 0; i < dnsMessage.dnsHeader_.arcount_; i++) {
            dnsMessage.additional_.add(DNSRecord.decodeRecord(inputStream,dnsMessage));
        }

        return dnsMessage;
    }
    String[] readDomainName(InputStream input) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(input);
        byte length = -1;
        byte[] chars;
        ArrayList<String> url = new ArrayList<>();
        while(length != 0) {
            length = dataInputStream.readByte();
            if (length != 0) {
                chars = dataInputStream.readNBytes(length);
                url.add(new String(chars));
            }
        }
        return url.toArray(new String[0]);
    }
    String[] readDomainName(int firstByte) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(completeMessage_));
        dataInputStream.readNBytes(firstByte);
        return readDomainName(dataInputStream);
    }
    static DNSMessage buildResponse(DNSMessage request, DNSRecord[] answers){
        DNSMessage response = new DNSMessage();
        response.dnsHeader_ = DNSHeader.buildHeaderForResponse(request, response);
        response.questions_ = request.questions_;
        response.answers_.addAll(Arrays.asList(answers));
        response.authorities_ = request.authorities_;
        response.additional_ = request.additional_;

        return response;
    }
//
    byte[] toBytes() throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        this.dnsHeader_.writeBytes(byteOutput);
        HashMap<String,Integer> domainNameLocations = new HashMap<>();

        for(DNSQuestion  question : questions_){
            question.writeBytes(byteOutput, domainNameLocations);
        }
        for(DNSRecord answer : answers_){
            answer.writeBytes(byteOutput, domainNameLocations);
        }
        for(DNSRecord au : authorities_){
            au.writeBytes(byteOutput,domainNameLocations);
        }
        for(DNSRecord ad : additional_){
            ad.writeBytes(byteOutput,domainNameLocations);
        }

        return byteOutput.toByteArray();



    }
    //If this is the first time we've seen this domain name in the packet,
    // write it using the DNS encoding (each segment of the domain prefixed with its length, 0 at the end),
    // and add it to the hash map. Otherwise, write a back pointer to where the domain has been seen previously.
    static void writeDomainName(ByteArrayOutputStream boutput, HashMap<String,Integer> domainLocations, String[] domainPieces) throws IOException {
        DataOutputStream dOutput = new DataOutputStream(boutput);
        String domainName = joinDomainName(domainPieces);

        if(!domainLocations.containsKey(domainName)){
            domainLocations.put(domainName, boutput.toByteArray().length);

            for(String s : domainPieces){
                dOutput.writeByte(s.length());
                dOutput.write(s.getBytes());
            }
            dOutput.writeByte(0);
        }
        else{
            int offset = domainLocations.get(domainName);
            short compression = (short) (offset | 0xC000);
            dOutput.writeShort(compression);
        }
    }

    static String joinDomainName(String[] pieces){
        StringBuilder s = new StringBuilder(new String());
        for(int i = 0; i < pieces.length; i++){
            s.append(pieces[i]);
            if(i != pieces.length-1){
                s.append(".");
            }
        }
        return s.toString();
    }
}
