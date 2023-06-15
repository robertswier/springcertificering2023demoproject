package com.example.demo.controllers;

import com.example.demo.model.CashCard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
/*
test of omzetten json <=> object goed gaat
 */
@JsonTest
public class CashCardJsonTest {

    @Autowired
    private JacksonTester<CashCard> json;

    /*
    File expected.json wordt ingelezen. Deze file bevat de json zoals die verwacht wordt als je het cashCard
     object omzet naar een json.

     */
    @Test
    public void cashCardSerializationTest() throws IOException {
        CashCard cashCard = new CashCard(99L, 123.45);
//        Deze file word tin hetzelfde folderpad gezocht als de package
        assertThat(json.write(cashCard)).isStrictlyEqualToJson("expected.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount")
                .isEqualTo(123.45);
    }

    /*
    Serialize json naar object en test het object
    Let op hoe json wordt geparsed naar object
     */
    @Test
    public void cashCardDeserializationTest() throws IOException {
        String expected = """
                {
                    "id": 99,
                    "amount": 123.45
                }
                """;
        assertThat(json.parse(expected))
                .isEqualTo(new CashCard(99L, 123.45));
        assertThat(json.parseObject(expected).id()).isEqualTo(99);
        assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);
    }
}
