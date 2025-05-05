package cn.coderule.minimq.domain.domain.enums.code;

import lombok.Getter;

@Getter
public enum InvalidCode {
    /**
     * <code>CODE_UNSPECIFIED = 0;</code>
     */
    CODE_UNSPECIFIED(0),
    /**
     * <pre>
     * Generic code for success.
     * </pre>
     *
     * <code>OK = 20000;</code>
     */
    OK(20000),
    /**
     * <pre>
     * Generic code for multiple return results.
     * </pre>
     *
     * <code>MULTIPLE_RESULTS = 30000;</code>
     */
    MULTIPLE_RESULTS(30000),
    /**
     * <pre>
     * Generic code for bad request, indicating that required fields or headers are missing.
     * </pre>
     *
     * <code>BAD_REQUEST = 40000;</code>
     */
    BAD_REQUEST(40000),
    /**
     * <pre>
     * Format of access point is illegal.
     * </pre>
     *
     * <code>ILLEGAL_ACCESS_POINT = 40001;</code>
     */
    ILLEGAL_ACCESS_POINT(40001),
    /**
     * <pre>
     * Format of topic is illegal.
     * </pre>
     *
     * <code>ILLEGAL_TOPIC = 40002;</code>
     */
    ILLEGAL_TOPIC(40002),
    /**
     * <pre>
     * Format of consumer group is illegal.
     * </pre>
     *
     * <code>ILLEGAL_CONSUMER_GROUP = 40003;</code>
     */
    ILLEGAL_CONSUMER_GROUP(40003),
    /**
     * <pre>
     * Format of message tag is illegal.
     * </pre>
     *
     * <code>ILLEGAL_MESSAGE_TAG = 40004;</code>
     */
    ILLEGAL_MESSAGE_TAG(40004),
    /**
     * <pre>
     * Format of message key is illegal.
     * </pre>
     *
     * <code>ILLEGAL_MESSAGE_KEY = 40005;</code>
     */
    ILLEGAL_MESSAGE_KEY(40005),
    /**
     * <pre>
     * Format of message group is illegal.
     * </pre>
     *
     * <code>ILLEGAL_MESSAGE_GROUP = 40006;</code>
     */
    ILLEGAL_MESSAGE_GROUP(40006),
    /**
     * <pre>
     * Format of message property key is illegal.
     * </pre>
     *
     * <code>ILLEGAL_MESSAGE_PROPERTY_KEY = 40007;</code>
     */
    ILLEGAL_MESSAGE_PROPERTY_KEY(40007),
    /**
     * <pre>
     * Transaction id is invalid.
     * </pre>
     *
     * <code>INVALID_TRANSACTION_ID = 40008;</code>
     */
    INVALID_TRANSACTION_ID(40008),
    /**
     * <pre>
     * Format of message id is illegal.
     * </pre>
     *
     * <code>ILLEGAL_MESSAGE_ID = 40009;</code>
     */
    ILLEGAL_MESSAGE_ID(40009),
    /**
     * <pre>
     * Format of filter expression is illegal.
     * </pre>
     *
     * <code>ILLEGAL_FILTER_EXPRESSION = 40010;</code>
     */
    ILLEGAL_FILTER_EXPRESSION(40010),
    /**
     * <pre>
     * The invisible time of request is invalid.
     * </pre>
     *
     * <code>ILLEGAL_INVISIBLE_TIME = 40011;</code>
     */
    ILLEGAL_INVISIBLE_TIME(40011),
    /**
     * <pre>
     * The delivery timestamp of message is invalid.
     * </pre>
     *
     * <code>ILLEGAL_DELIVERY_TIME = 40012;</code>
     */
    ILLEGAL_DELIVERY_TIME(40012),
    /**
     * <pre>
     * Receipt handle of message is invalid.
     * </pre>
     *
     * <code>INVALID_RECEIPT_HANDLE = 40013;</code>
     */
    INVALID_RECEIPT_HANDLE(40013),
    /**
     * <pre>
     * Message property conflicts with its type.
     * </pre>
     *
     * <code>MESSAGE_PROPERTY_CONFLICT_WITH_TYPE = 40014;</code>
     */
    MESSAGE_PROPERTY_CONFLICT_WITH_TYPE(40014),
    /**
     * <pre>
     * Client type could not be recognized.
     * </pre>
     *
     * <code>UNRECOGNIZED_CLIENT_TYPE = 40015;</code>
     */
    UNRECOGNIZED_CLIENT_TYPE(40015),
    /**
     * <pre>
     * Message is corrupted.
     * </pre>
     *
     * <code>MESSAGE_CORRUPTED = 40016;</code>
     */
    MESSAGE_CORRUPTED(40016),
    /**
     * <pre>
     * Request is rejected due to missing of x-mq-client-id header.
     * </pre>
     *
     * <code>CLIENT_ID_REQUIRED = 40017;</code>
     */
    CLIENT_ID_REQUIRED(40017),
    /**
     * <pre>
     * Polling time is illegal.
     * </pre>
     *
     * <code>ILLEGAL_POLLING_TIME = 40018;</code>
     */
    ILLEGAL_POLLING_TIME(40018),
    /**
     * <pre>
     * Offset is illegal.
     * </pre>
     *
     * <code>ILLEGAL_OFFSET = 40019;</code>
     */
    ILLEGAL_OFFSET(40019),
    /**
     * <pre>
     * Generic code indicates that the client request lacks valid authentication
     * credentials for the requested resource.
     * </pre>
     *
     * <code>UNAUTHORIZED = 40100;</code>
     */
    UNAUTHORIZED(40100),
    /**
     * <pre>
     * Generic code indicates that the account is suspended due to overdue of payment.
     * </pre>
     *
     * <code>PAYMENT_REQUIRED = 40200;</code>
     */
    PAYMENT_REQUIRED(40200),
    /**
     * <pre>
     * Generic code for the case that user does not have the permission to operate.
     * </pre>
     *
     * <code>FORBIDDEN = 40300;</code>
     */
    FORBIDDEN(40300),
    /**
     * <pre>
     * Generic code for resource not found.
     * </pre>
     *
     * <code>NOT_FOUND = 40400;</code>
     */
    NOT_FOUND(40400),
    /**
     * <pre>
     * Message not found from server.
     * </pre>
     *
     * <code>MESSAGE_NOT_FOUND = 40401;</code>
     */
    MESSAGE_NOT_FOUND(40401),
    /**
     * <pre>
     * Topic resource does not exist.
     * </pre>
     *
     * <code>TOPIC_NOT_FOUND = 40402;</code>
     */
    TOPIC_NOT_FOUND(40402),
    /**
     * <pre>
     * Consumer group resource does not exist.
     * </pre>
     *
     * <code>CONSUMER_GROUP_NOT_FOUND = 40403;</code>
     */
    CONSUMER_GROUP_NOT_FOUND(40403),
    /**
     * <pre>
     * Offset not found from server.
     * </pre>
     *
     * <code>OFFSET_NOT_FOUND = 40404;</code>
     */
    OFFSET_NOT_FOUND(40404),
    /**
     * <pre>
     * Generic code representing client side timeout when connecting to, reading data from, or write data to server.
     * </pre>
     *
     * <code>REQUEST_TIMEOUT = 40800;</code>
     */
    REQUEST_TIMEOUT(40800),
    /**
     * <pre>
     * Generic code represents that the request entity is larger than limits defined by server.
     * </pre>
     *
     * <code>PAYLOAD_TOO_LARGE = 41300;</code>
     */
    PAYLOAD_TOO_LARGE(41300),
    /**
     * <pre>
     * Message body size exceeds the threshold.
     * </pre>
     *
     * <code>MESSAGE_BODY_TOO_LARGE = 41301;</code>
     */
    MESSAGE_BODY_TOO_LARGE(41301),
    /**
     * <pre>
     * Generic code for use cases where pre-conditions are not met.
     * For example, if a producer instance is used to publish messages without prior start() invocation,
     * this error code will be raised.
     * </pre>
     *
     * <code>PRECONDITION_FAILED = 42800;</code>
     */
    PRECONDITION_FAILED(42800),
    /**
     * <pre>
     * Generic code indicates that too many requests are made in short period of duration.
     * Requests are throttled.
     * </pre>
     *
     * <code>TOO_MANY_REQUESTS = 42900;</code>
     */
    TOO_MANY_REQUESTS(42900),
    /**
     * <pre>
     * Generic code for the case that the server is unwilling to process the request because its header fields are too large.
     * The request may be resubmitted after reducing the size of the request header fields.
     * </pre>
     *
     * <code>REQUEST_HEADER_FIELDS_TOO_LARGE = 43100;</code>
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(43100),
    /**
     * <pre>
     * Message properties total size exceeds the threshold.
     * </pre>
     *
     * <code>MESSAGE_PROPERTIES_TOO_LARGE = 43101;</code>
     */
    MESSAGE_PROPERTIES_TOO_LARGE(43101),
    /**
     * <pre>
     * Generic code indicates that server/client encountered an unexpected
     * condition that prevented it from fulfilling the request.
     * </pre>
     *
     * <code>INTERNAL_ERROR = 50000;</code>
     */
    INTERNAL_ERROR(50000),
    /**
     * <pre>
     * Code indicates that the server encountered an unexpected condition
     * that prevented it from fulfilling the request.
     * This error response is a generic "catch-all" response.
     * Usually, this indicates the server cannot find a better alternative
     * error code to response. Sometimes, server administrators log error
     * responses like the 500 status code with more details about the request
     * to prevent the error from happening again in the future.
     * See https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/500
     * </pre>
     *
     * <code>INTERNAL_SERVER_ERROR = 50001;</code>
     */
    INTERNAL_SERVER_ERROR(50001),
    /**
     * <pre>
     * The HA-mechanism is not working now.
     * </pre>
     *
     * <code>HA_NOT_AVAILABLE = 50002;</code>
     */
    HA_NOT_AVAILABLE(50002),
    /**
     * <pre>
     * Generic code means that the server or client does not support the
     * functionality required to fulfill the request.
     * </pre>
     *
     * <code>NOT_IMPLEMENTED = 50100;</code>
     */
    NOT_IMPLEMENTED(50100),
    /**
     * <pre>
     * Generic code represents that the server, which acts as a gateway or proxy,
     * does not get an satisfied response in time from its upstream servers.
     * See https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/504
     * </pre>
     *
     * <code>PROXY_TIMEOUT = 50400;</code>
     */
    PROXY_TIMEOUT(50400),
    /**
     * <pre>
     * Message persistence timeout.
     * </pre>
     *
     * <code>MASTER_PERSISTENCE_TIMEOUT = 50401;</code>
     */
    MASTER_PERSISTENCE_TIMEOUT(50401),
    /**
     * <pre>
     * Slave persistence timeout.
     * </pre>
     *
     * <code>SLAVE_PERSISTENCE_TIMEOUT = 50402;</code>
     */
    SLAVE_PERSISTENCE_TIMEOUT(50402),
    /**
     * <pre>
     * Generic code for unsupported operation.
     * </pre>
     *
     * <code>UNSUPPORTED = 50500;</code>
     */
    UNSUPPORTED(50500),
    /**
     * <pre>
     * Operation is not allowed in current version.
     * </pre>
     *
     * <code>VERSION_UNSUPPORTED = 50501;</code>
     */
    VERSION_UNSUPPORTED(50501),
    /**
     * <pre>
     * Not allowed to verify message. Chances are that you are verifying
     * a FIFO message, as is violating FIFO semantics.
     * </pre>
     *
     * <code>VERIFY_FIFO_MESSAGE_UNSUPPORTED = 50502;</code>
     */
    VERIFY_FIFO_MESSAGE_UNSUPPORTED(50502),
    /**
     * <pre>
     * Generic code for failed message consumption.
     * </pre>
     *
     * <code>FAILED_TO_CONSUME_MESSAGE = 60000;</code>
     */
    FAILED_TO_CONSUME_MESSAGE(60000),
    UNRECOGNIZED(-1),
    ;


    private final int code;
    InvalidCode(int code) {
        this.code = code;
    }
}
