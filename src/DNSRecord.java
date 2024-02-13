import java.io.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class DNSRecord {

    // Answer


    @Override
    public String toString() {
        return "DNSRecord{" +
                "name_=" + Arrays.toString(name_) +
                ", type_=" + type_ +
                ", class_l=" + class_ +
                ", ttl_=" + ttl_ +
                ", rdLength_=" + rdLength_ +
                ", rdata_=" + Arrays.toString(rdata_) +
                ", restOfData=" + Arrays.toString(restOfData) +
                '}';
    }

    String[]  name_;
    short type_;
    short class_;
    int ttl_;
    short rdLength_;
    byte[] rdata_;

    long expireTime;


    byte[] restOfData;
    static DNSRecord decodeRecord(InputStream input , DNSMessage dnsm) throws IOException {
        DNSRecord dnsRecord = new DNSRecord();
        DataInputStream dataInputStream = new DataInputStream(input);
        if(dnsm.hasAnswer_){
//            System.out.println("Has answer");
            //check if it's compressed
            dataInputStream.mark(2);
            short first2Btye = dataInputStream.readShort();

            boolean IsCompressed = (first2Btye & 0xC000 ) == 0xC000;
            if(IsCompressed){
//                System.out.println("Is compressed");
                dnsRecord.name_ = dnsm.readDomainName(first2Btye & 0x3fff);
            }
            else{
//                System.out.println("Is not compressed");
                dataInputStream.reset();
                dnsRecord.name_ = dnsm.readDomainName(dataInputStream);
            }
            dnsRecord.type_ = dataInputStream.readShort();
            dnsRecord.class_ = dataInputStream.readShort();
            dnsRecord.ttl_ = dataInputStream.readInt();
            dnsRecord.rdLength_ = dataInputStream.readShort();
            dnsRecord.rdata_ = dataInputStream.readNBytes(dnsRecord.rdLength_);

            Date time = new Date();
            dnsRecord.expireTime = time.getTime() + dnsRecord.ttl_ * 1000L;
//            System.out.println("expire time = " + dnsRecord.expireTime + "(" + time.getTime() + " + " + dnsRecord.ttl_ * 1000L + ")");


        }
        else{
//            System.out.println("No answer");
            dnsRecord.restOfData = dataInputStream.readNBytes(11);
        }
//        System.out.println(dnsRecord.toString());
        return  dnsRecord;

    }
    void writeBytes(ByteArrayOutputStream bOutput, HashMap<String, Integer> map) throws IOException {
        DataOutputStream dOutput = new DataOutputStream(bOutput);
        if(name_ != null) {
            DNSMessage.writeDomainName(bOutput, map, name_);
            dOutput.writeShort(type_);
            dOutput.writeShort(class_);
            dOutput.writeInt(ttl_);
            dOutput.writeShort(rdLength_);
            dOutput.write(rdata_);
        }
        else {
            if (restOfData != null) {
                dOutput.write(restOfData);
            }
        }
    }

    boolean isExpired(){
        Date currentTime = new Date();
        System.out.println("Curren time = " + currentTime.getTime());
        System.out.println("Expire Time = " + expireTime);
        return currentTime.getTime() > expireTime;
    }

}
