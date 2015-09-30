
package com.ekuater.labelchat.im.message;

import com.ekuater.labelchat.im.PacketStruct;

/**
 * @author LinYong
 */
public class InstructionMessage extends JsonMessage {

    public InstructionMessage() {
        super(PacketStruct.TYPE_INSTRUCTION);
    }
}
