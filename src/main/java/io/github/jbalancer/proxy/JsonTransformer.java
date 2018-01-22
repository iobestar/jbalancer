package io.github.jbalancer.proxy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import spark.ResponseTransformer;

import java.io.IOException;

@Component
public class JsonTransformer implements ResponseTransformer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    }

    @Override
    public String render(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    public <T> T deserialize(String value, Class<T> tClass) {

        try {
            return objectMapper.readValue(value, tClass);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }
}
