package com.relyon.financiallife.mapper.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalDateDeserializerTest {

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext context;

    private final LocalDateDeserializer deserializer = new LocalDateDeserializer();

    @Test
    void deserialize_WithValidDateString_ShouldReturnValidLocalDate() throws IOException {
        String dateString = "22/04/2023";

        when(jsonParser.getText()).thenReturn(dateString);

        LocalDate result = deserializer.deserialize(jsonParser, context);

        assertEquals(LocalDate.of(2023, 4, 22), result);
    }
}