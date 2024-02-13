import java.util.HashMap;

public class DNSCache {

    HashMap<DNSQuestion, DNSRecord> table_;

    DNSCache(){
        table_ = new HashMap<>();
    }

    boolean contains(DNSMessage dnsMessage){
        System.out.println("checking questions : " + dnsMessage.questions_.get(0));
        if (table_.containsKey(dnsMessage.questions_.get(0))){
            System.out.println("Already in the cache");
            if(!table_.get(dnsMessage.questions_.get(0)).isExpired())
                return true;
            else {
                System.out.println("cache expired");
                table_.remove(dnsMessage.questions_.get(0));
            }
        }
        return false;
    }

    void add(DNSMessage dnsMessage){

        table_.put(dnsMessage.questions_.get(0), dnsMessage.answers_.get(0));
        System.out.println("new add questions : " + dnsMessage.questions_.get(0));
    }

    public DNSRecord[] getAnswer(DNSMessage dnsMessage){
        return new DNSRecord[]{table_.get(dnsMessage.questions_.get(0))};
    }
}
