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