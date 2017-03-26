package server;

/**
 *
 * @author arman
 */
final public class FishDBException extends Exception {
    private static final long serialVersionUID = -41424240839829672L;

    /**
     *
     * @param reason
     */
    public FishDBException(String reason) {
        super(reason);
    }

    /**
     *
     * @param reason
     * @param rootCause
     */
    public FishDBException(String reason, Throwable rootCause) {
        super(reason, rootCause);
    }
}
