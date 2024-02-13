import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class DNSQuestion {

    String[] DomainName_;
    short qtype_;
    short qclass_;

    @Override
    public String toString() {
        return "DNSQuestion{" +
                "DomainName_=" + Arrays.toString(DomainName_) +
                ", qtype_=" + qtype_ +
                ", qclass_=" + qclass_ +
                '}';
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DNSQuestion that = (DNSQuestion) o;
        return qtype_ == that.qtype_ && qclass_ == that.qclass_ && Arrays.equals(DomainName_, that.DomainName_);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(qtype_, qclass_);
        result = 31 * result + Arrays.hashCode(DomainName_);
        return result;
    }




    static DNSQuestion decodeQuestion(InputStream input, DNSMessage dnsMessage) throws IOException {
        DNSQuestion dnsq = new DNSQuestion();
        // read question
        dnsq.DomainName_ = dnsMessage.readDomainName(input);

        DataInputStream dataInputStream = new DataInputStream(input);
        // read qtype and qclass
        dnsq.qtype_ = dataInputStream.readShort();
        dnsq.qclass_ = dataInputStream.readShort();
//        System.out.println(dnsq.toString());
        return dnsq;

    }

    void writeBytes(ByteArrayOutputStream output, HashMap<String,Integer> domainNameLocations) throws IOException {

        DNSMessage.writeDomainName(output,domainNameLocations, DomainName_);

        DataOutputStream dOutput = new DataOutputStream(output);
        dOutput.writeShort(qtype_);
        dOutput.writeShort(qclass_);

    }
}
