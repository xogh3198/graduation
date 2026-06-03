package com.project.graduation.dto.control;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ControlResponse {
    private final String status;
    private final String message;
    private final Integer amount;
    private final Integer brightnessPct;

    public ControlResponse(String status, String message) {
        this(status, message, null, null);
    }

    public ControlResponse(String status, String message, Integer amount, Integer brightnessPct) {
        this.status = status;
        this.message = message;
        this.amount = amount;
        this.brightnessPct = brightnessPct;
    }

    public static ControlResponse waterSuccess(int amount) {
        return new ControlResponse(
                "success",
                amount + "ml 급수 명령을 전송했습니다.",
                amount,
                null
        );
    }

    public static ControlResponse ledSuccess(String message, int brightnessPct) {
        return new ControlResponse("success", message, null, brightnessPct);
    }
}
