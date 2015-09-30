
package com.ekuater.labelchat.im;

public class IMException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 2347439838919809784L;

    public IMException(Throwable wrappedThrowable) {
        super(wrappedThrowable);
    }

    public IMException(String message) {
        super(message);
    }

    public IMException(String message, Throwable wrappedThrowable) {
        super(message, wrappedThrowable);
    }

    protected IMException() {
    }

    public static class NotConnectedException extends IMException {

        /**
         * 
         */
        private static final long serialVersionUID = -6741803807144403105L;

        public NotConnectedException() {
        }
    }

    public static class PacketParserException extends IMException {

        /**
         * 
         */
        private static final long serialVersionUID = -7907326403178741309L;

        public PacketParserException() {
        }

    }
}
