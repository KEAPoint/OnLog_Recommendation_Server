package keapoint.onlog.recommendation.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BaseErrorCode {

    /**
     * 500 : INTERNAL SERVER ERROR
     */
    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "예상치 못한 에러가 발생하였습니다.");

    private final Integer status;
    private final String message;
}
