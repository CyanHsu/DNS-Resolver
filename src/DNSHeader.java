import java.io.*;

public class DNSHeader {

    @Override
    public String toString() {
        return "DNSHeader{" +
                "id_=" + Integer.toHexString(id_ & 0xffff) +
                ", qr_=" + (qr_?1:0) +
                ", opcode_=" + opcode_ +
                ", aa_=" + (aa_?1:0) +
                ", tc_=" + (tc_?1:0) +
                ", rd_=" + (rd_?1:0) +
                ", ra_=" + (ra_?1:0) +
                ", z_=" + (z_?1:0) +
                ", ad_=" + (ad_?1:0) +
                ", cd_=" + (cd_?1:0) +
                ", rcode_=" + Byte.toString(rcode_) +
                ", QDCOUNT_=" + qdcount_ +
                ", ANCOUNT_=" + ancount_ +
                ", NSCOUNT_=" + nscount_ +
                ", ARCOUNT_=" + arcount_ +
                '}';
    }

     short id_;
     boolean qr_;
     byte opcode_;
     boolean aa_;
     boolean tc_;
     boolean rd_;
     boolean ra_;
     boolean z_;
     boolean ad_;
     boolean cd_;
     byte rcode_;

     short qdcount_;
     short ancount_;
     short nscount_;
     short arcount_;






    //--read the header from an input stream (we'll use a ByteArrayInputStream but we will only use the basic read methods
    // of input stream to read 1 byte, or to fill in a byte array, so we'll be generic).
    static DNSHeader decodeHeader(InputStream input) throws IOException {

        DataInputStream readData = new DataInputStream(input);
        DNSHeader header = new DNSHeader();
//        header.input_ = input;

        //ID - first 2 bytes
        header.id_ = readData.readShort();

        // 3rd byte
        byte thirdByte = readData.readByte();

        header.qr_= ((thirdByte & 0x80) > 0);
        header.opcode_ = (byte) ((thirdByte & 0x78) >> 3);
        header.aa_ = ( thirdByte & 0x04) > 0;
        header.tc_ = (( thirdByte & 0x02) >> 1) > 0;
        header.rd_ = ( thirdByte & 0x01) > 0;
        //4th byte
        byte fourthByte = readData.readByte();
        header.ra_ = (fourthByte & 0x80) > 0;
        header.z_ = (fourthByte & 0x40) > 0;
        header.ad_= (fourthByte & 0x20) > 0;
        header.cd_= (fourthByte & 0x10) > 0;
        header.rcode_ = (byte) (fourthByte & 0x0f);

        header.qdcount_ = readData.readShort();
        header.ancount_ = readData.readShort();
        header.nscount_ = readData.readShort();
        header.arcount_ = readData.readShort();

//        System.out.println("ID: " + Integer.toHexString(header.id_ & 0xffff ));
//        System.out.println(header.toString());

        return header;
    }
    static DNSHeader buildHeaderForResponse(DNSMessage request, DNSMessage response){
        DNSHeader responseHeader  = request.dnsHeader_;
        responseHeader.qr_ = true;
        responseHeader.ra_ = true;
        responseHeader.ancount_ = 1;

        return responseHeader;
    }
    void writeBytes(OutputStream output) throws IOException {
        DataOutputStream dOutput = new DataOutputStream(output);
        dOutput.writeShort(id_);
        byte thirdByte = 0;
        if(qr_){
            thirdByte = (byte) 0x80;
        }
        if (opcode_ != 0){
            thirdByte = (byte) (thirdByte | (opcode_ << 3));
        }
        if (aa_){
            thirdByte = (byte) (thirdByte | 0x04);
        }
        if (tc_){
            thirdByte = (byte) (thirdByte | 0x02);
        }
        if (rd_) {
            thirdByte = (byte) (thirdByte | 0x01);
        }
        dOutput.writeByte(thirdByte);

        byte fourthByte = 0;
        if (ra_){
            fourthByte = (byte) 0x80;
        }
        if (z_){
            fourthByte = (byte) (fourthByte | 0x40);
        }
        if(ad_){
            fourthByte = (byte) (fourthByte | 0x20);
        }
        if(cd_){
            fourthByte = (byte) (fourthByte | 0x10);
        }
        if(rcode_ != 0){
            fourthByte = (byte) (fourthByte | rcode_);
        }
        dOutput.writeByte(fourthByte);

        dOutput.writeShort(qdcount_);
        dOutput.writeShort(ancount_);
        dOutput.writeShort(nscount_);
        dOutput.writeShort(arcount_);

    }


}
