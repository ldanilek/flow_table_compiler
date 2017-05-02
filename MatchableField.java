
/* Every column of a flowtable must match on either a packet field or some
 * variable
 */
class MatchableField{
    private final PacketField packetField;
    private final String variableField;

    public MatchableField(PacketField packetField){
        this.packetField = packetField;
        this.variableField = null;
    }
    public MatchableField(String variableField){
        this.packetField = null;
        this.variableField = variableField;
    }
    public PacketField getPacketField(){
        return packetField;
    }
    public String getVariableField(){
        return variableField;
    }

    public String printable(){
        if (packetField != null)
          return packetField.name();
        else
          return variableField;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null)
            return false;
        else if(!(obj instanceof MatchableField))
            return false;
        else {
            MatchableField toCompare = (MatchableField) obj;
            return (this.variableField == toCompare.variableField
                || this.packetField == toCompare.packetField);
        }
    }
}

enum PacketField {
    // immediate, one-hop, source and destination
    EthernetSrc, EthernetDst,
    // in/out port
    InPort, OutPort,
    // original/eventual source and destination
    IPv4Src, IPv4Dst,
    // time-to-live counter
    TTL
}
