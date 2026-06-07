package com.plantify.payment.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plantify.payment.global.exception.ApplicationException;
import com.plantify.payment.global.exception.errorcode.FeignErrorCode;
import com.plantify.payment.global.response.ApiResponse;
import feign.Response;
import feign.codec.Decoder;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.lang.reflect.Type;

@RequiredArgsConstructor
public class FeignApiResponseDecoder implements Decoder {

    private final ObjectMapper objectMapper;

    @Override
    public Object decode(Response response, Type type) throws IOException {
        if (response.body() == null) {
            return null;
        }

        JavaType apiResponseType = objectMapper.getTypeFactory()
                .constructParametricType(ApiResponse.class, Object.class);
        ApiResponse<?> apiResponse = objectMapper.readValue(
                response.body().asInputStream(), apiResponseType
        );

        if (isApiResponseType(type)) {
            return apiResponse;
        }

        if (!apiResponse.success()) {
            FeignErrorCode errorCode = FeignErrorCode.of(response.status());
            throw new ApplicationException(errorCode, apiResponse.message());
        }

        if (apiResponse.data() == null) {
            return null;
        }

        JavaType targetType = objectMapper.getTypeFactory().constructType(type);
        return objectMapper.convertValue(apiResponse.data(), targetType);
    }

    private boolean isApiResponseType(Type type) {
        JavaType targetType = objectMapper.getTypeFactory().constructType(type);
        return ApiResponse.class.isAssignableFrom(targetType.getRawClass());
    }
}
