package ru.antipant;

import com.codeborne.pdftest.PDF;
import com.codeborne.pdftest.matchers.ContainsExactText;
import com.codeborne.selenide.Selenide;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;
import static org.hamcrest.MatcherAssert.assertThat;

public class SelenideDownloadTest {
    ClassLoader cl = SelenideDownloadTest.class.getClassLoader();
    String pdfName = "junit-user-guide-5.8.2.pdf";
    String xlsxName = "example.xlsx";
    String csvName = "student.csv";
    String zipName = "sample.zip";
    String jsonName = "example.json";

    @Test
    void downloadTest() throws Exception {
        Selenide.open("https://github.com/junit-team/junit5/blob/main/README.md");
        File textFile = $("#raw-url").download();
        try (InputStream is = new FileInputStream(textFile)) {
            byte[] fileContent = is.readAllBytes();
            String strContent = new String(fileContent, StandardCharsets.UTF_8);
            org.assertj.core.api.Assertions.assertThat(strContent).contains("JUnit 5");
        }
    }

    @Test
    void pdfParsingTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("pdf/" + pdfName)) {
            assert stream != null;
            PDF pdf = new PDF(stream);
            Assertions.assertEquals(166, pdf.numberOfPages);
            assertThat(pdf, new ContainsExactText("123"));
        }
    }

    @Test
    void xlsParsingTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("xls/" + xlsxName)) {
            assert stream != null;
            XLS xls = new XLS(stream);
            String stringCellValue = xls.excel.getSheetAt(0).getRow(1).getCell(0).getStringCellValue();
            org.assertj.core.api.Assertions.assertThat(stringCellValue).contains("Краснодарский край");
        }
    }

    @Test
    void csvParsingTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("csv/" + csvName)) {
            assert stream != null;
            try (CSVReader reader = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

                List<String[]> content = reader.readAll();
                org.assertj.core.api.Assertions.assertThat(content).contains(
                        new String[]{"Name", "Surname"},
                        new String[]{"Ivan", "Ivanov"},
                        new String[]{"Petr", "Petrov"}
                );
            }
        }
    }

    @Test
    void zipParsingTest() throws Exception {
        ZipFile zf = new ZipFile(new File("src/test/resources/zip/" + zipName));
        ZipInputStream is = new ZipInputStream(Objects.requireNonNull(cl.getResourceAsStream("zip/" + zipName)));
        ZipEntry entry;
        while ((entry = is.getNextEntry()) != null) {
            if (entry.getName().equals(pdfName)) {
                try (InputStream stream = zf.getInputStream(entry)) {
                    assert stream != null;
                    PDF pdf = new PDF(stream);
                    Assertions.assertEquals(166, pdf.numberOfPages);
                    assertThat(pdf, new ContainsExactText("123"));
                }
            }
            if (entry.getName().equals(csvName)) {
                try (InputStream stream = zf.getInputStream(entry)) {
                    assert stream != null;
                    try (CSVReader reader = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

                        List<String[]> content = reader.readAll();
                        org.assertj.core.api.Assertions.assertThat(content).contains(
                                new String[]{"Name", "Surname"},
                                new String[]{"Ivan", "Ivanov"},
                                new String[]{"Petr", "Petrov"}
                        );
                    }
                }
            }
            if (entry.getName().equals(xlsxName)) {
                try (InputStream stream = zf.getInputStream(entry)) {
                    assert stream != null;
                    XLS xls = new XLS(stream);
                    String stringCellValue = xls.excel.getSheetAt(0).getRow(1).getCell(0).getStringCellValue();
                    org.assertj.core.api.Assertions.assertThat(stringCellValue).contains("Краснодарский край");
                }
            }
        }
    }

    @Test
    void jsonGsonParsingTest() {
        Gson g = new Gson();
        Person person = g.fromJson(JsonToString.readJsonData("src/test/resources/json/" + jsonName), Person.class);
        org.assertj.core.api.Assertions.assertThat(person.firstName).contains("John");
        org.assertj.core.api.Assertions.assertThat(person.lastName).contains("doe");
        org.assertj.core.api.Assertions.assertThat(person.address.streetAddress).contains("naist street");
    }

    @Test
    void jsonJacksonParsingTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Person person = mapper.readValue(JsonToString.readJsonData("src/test/resources/json/" + jsonName), Person.class);
        org.assertj.core.api.Assertions.assertThat(person.firstName).contains("John");
        org.assertj.core.api.Assertions.assertThat(person.lastName).contains("doe");
        org.assertj.core.api.Assertions.assertThat(person.address.streetAddress).contains("naist street");
    }

}