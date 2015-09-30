package com.ekuater.httpfileloader.disc.naming;

/**
 * Generates names for files at disk cache
 */
public interface FileNameGenerator {

    /**
     * Generates unique file name for file defined by URI
     */
    String generate(String fileUri);
}
