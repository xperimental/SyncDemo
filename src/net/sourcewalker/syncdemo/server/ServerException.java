package net.sourcewalker.syncdemo.server;

public class ServerException extends Exception {

    private static final long serialVersionUID = -2741338001747740296L;

    public ServerException() {
        super();
    }

    public ServerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ServerException(String detailMessage) {
        super(detailMessage);
    }

    public ServerException(Throwable throwable) {
        super(throwable);
    }

}
