
package com.ekuater.labelchat.im;

/**
 * packet structure
 * 
 * @author LinYong
 */
public final class PacketStruct {

    public static final int PROTOCOL_VERSION = 0x01;
    public static final String CHARSET_UTF_8_NAME = "UTF-8";

    public static final int HEADER_PACKET_TOTAL_LEN_PART_LEN = 4;

    public static final int HEADER_PROTOCOL_VERSION_PART_LEN = 1;
    public static final int HEADER_ENCRYPT_VERSION_PART_LEN = 1;
    public static final int HEADER_PIPELINE_NUMBER_PART_LEN = 4;
    public static final int HEADER_TYPE_PART_LEN = 1;
    public static final int HEADER_CHARSET_PART_LEN = 1;
    public static final int HEADER_SOURCE_ADDR_PART_LEN = 8;
    public static final int HEADER_DEST_ADDR_PART_LEN = 8;

    public static final int HEADER_TOTAL_LEN = 0
            + HEADER_PACKET_TOTAL_LEN_PART_LEN
            + HEADER_PROTOCOL_VERSION_PART_LEN
            + HEADER_ENCRYPT_VERSION_PART_LEN
            + HEADER_PIPELINE_NUMBER_PART_LEN
            + HEADER_TYPE_PART_LEN
            + HEADER_CHARSET_PART_LEN
            + HEADER_SOURCE_ADDR_PART_LEN
            + HEADER_DEST_ADDR_PART_LEN
            + 0;

    // Packet type
    public static final byte TYPE_PING = 0x00;
    public static final byte TYPE_MESSAGE = 0x01;
    public static final byte TYPE_REQUEST = 0x02;
    public static final byte TYPE_INSTRUCTION = 0x03;
    public static final byte TYPE_AUTHENTICATION = 0x04;
    public static final byte TYPE_NOTIFICATION = 0x05;

    // Encryption type
    public static final byte ENCRYPT_NONE = 0x00;
    public static final byte ENCRYPT_BASE64 = 0x01;
    public static final byte ENCRYPT_RSA = 0x02;
    public static final byte ENCRYPT_DES = 0x03;
    public static final byte ENCRYPT_BLOWFISH = 0x04;
    public static final byte ENCRYPT_MD5 = 0x05;
    public static final byte ENCRYPT_AES = 0x06;

    // Charset
    public static final byte CHARSET_BINARY = 0x00;
    public static final byte CHARSET_UTF_8 = 0x01;
}
