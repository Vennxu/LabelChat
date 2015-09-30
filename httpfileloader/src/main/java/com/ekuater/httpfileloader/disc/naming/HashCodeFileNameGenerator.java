package com.ekuater.httpfileloader.disc.naming;

/**
 * Names image file as image URI {@linkplain String#hashCode() hashcode}
 */
public class HashCodeFileNameGenerator implements FileNameGenerator {
    @Override
    public String generate(String fileUri) {
        return String.valueOf(fileUri.hashCode());
    }
}
