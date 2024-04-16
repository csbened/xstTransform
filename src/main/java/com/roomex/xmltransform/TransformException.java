package com.roomex.xmltransform;

public class TransformException extends RuntimeException {
    public TransformException(String message) {
        super(message);
    }
    public TransformException(Throwable t) {
        super(t);
    }
    public TransformException(String message, Throwable t) {
        super(message, t);
    }
}
