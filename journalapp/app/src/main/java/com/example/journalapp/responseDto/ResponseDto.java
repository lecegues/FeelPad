package com.example.journalapp.responseDto;

public class ResponseDto<T> {
    private final boolean success;
    private final String errorMessage;
    private final T data;

    private ResponseDto(boolean success, String errorMessage, T data) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public static <T> ResponseDto<T> createSuccessfulResponseDto(T data) {
        return new ResponseDto<>(true, null, data);
    }

    public static <T> ResponseDto<T> createErrorResponseDto(String errorMessage) {
        return new ResponseDto<>(false, errorMessage, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public T getData() {
        return data;
    }
}
