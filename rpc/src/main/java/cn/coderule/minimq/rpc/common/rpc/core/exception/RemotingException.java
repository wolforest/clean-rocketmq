package cn.coderule.minimq.rpc.common.rpc.core.exception;

public class RemotingException extends Exception {
    private static final long serialVersionUID = -5690687334570505110L;

    private int code = 0;

    public RemotingException(String message) {
        super(message);
    }

    public RemotingException(int code, String message) {
        super(message);
        this.code = code;
    }

    public RemotingException(int code, String message, String addr) {
        super(addr + ":" + message);
        this.code = code;
    }

    public RemotingException(String message, Throwable cause) {
        super(message, cause);
    }
}
