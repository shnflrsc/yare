
package io.shnflrsc.yare;

public class FileTooLargeException extends RuntimeException {
    public FileTooLargeException(String fileName) {
        super("File exceeds 100MB: " + fileName);
    }
}