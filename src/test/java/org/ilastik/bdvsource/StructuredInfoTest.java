/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ilastik.bdvsource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ilastik.bdvsource.StructuredInfo;
import static org.junit.Assert.*;

/**
 *
 * @author dkutra
 */
public class StructuredInfoTest {

    public StructuredInfoTest() {
    }

    @Test
    public void testStructuredInfoDeserializationSimple() throws Exception {
        String jsonString = String.join("",
        "{",
          "\"states\": [",
            "[",
              "{",
                "\"name\": \"ImageGroup\",",
                "\"axes\": \"tczyx\",",
                "\"shape\": [",
                  "1,",
                  "1,",
                  "1,",
                  "250,",
                  "250",
                "],",
                "\"dtype\": \"uint8\",",
                "\"version\": 0,",
                "\"lane_number\": 0,",
                "\"dataset_name\": \"testimg.1\",",
                "\"source_name\": \"ImageGroup\"",
              "}",
            "]",
          "],",
          "\"image_names\": [",
            "\"testimg.1\"",
          "],",
          "\"message\": \"Structured info retrieval successful\"",
        "}"
        );
        ObjectMapper objectMapper = new ObjectMapper();
        StructuredInfo loadedObject = objectMapper.readValue(jsonString, StructuredInfo.class);
        assertEquals(loadedObject.test_int, 1);
    }
 
    @Test
    public void testStructuredInfoDeserializationFull() throws Exception {
        String jsonString = String.join("",
        "{",
          "\"states\": [",
            "[",
              "{",
                "\"name\": \"ImageGroup\",",
                "\"axes\": \"tczyx\",",
                "\"shape\": [",
                  "1,",
                  "1,",
                  "1,",
                  "250,",
                  "250",
                "],",
                "\"dtype\": \"uint8\",",
                "\"version\": 0,",
                "\"lane_number\": 0,",
                "\"dataset_name\": \"testimg.1\",",
                "\"source_name\": \"ImageGroup\"",
              "},",
              "{",
                "\"name\": \"CachedPredictionImage\",",
                "\"axes\": \"tczyx\",",
                "\"shape\": [",
                  "1,",
                  "49,",
                  "1,",
                  "250,",
                  "250",
                "],",
                "\"dtype\": \"float32\",",
                "\"version\": 0,",
                "\"lane_number\": 0,",
                "\"dataset_name\": \"testimg.1\",",
                "\"source_name\": \"CachedPredictionImage\"",
              "}",
            "],",
            "[",
              "{",
                "\"name\": \"ImageGroup\",",
                "\"axes\": \"tczyx\",",
                "\"shape\": [",
                  "1,",
                  "1,",
                  "1,",
                  "250,",
                  "250",
                "],",
                "\"dtype\": \"uint8\",",
                "\"version\": 0,",
                "\"lane_number\": 1,",
                "\"dataset_name\": \"testimg.2\",",
                "\"source_name\": \"ImageGroup\"",
              "},",
              "{",
                "\"name\": \"CachedPredictionImage\",",
                "\"axes\": \"tczyx\",",
                "\"shape\": [",
                  "1,",
                  "49,",
                  "1,",
                  "250,",
                  "250",
                "],",
                "\"dtype\": \"float32\",",
                "\"version\": 0,",
                "\"lane_number\": 1,",
                "\"dataset_name\": \"testimg.2\",",
                "\"source_name\": \"CachedPredictionImage\"",
              "}",
          "],",
          "\"image_names\": [",
            "\"testimg.1\",",
            "\"testimg.2\"",
          "],",
          "\"message\": \"Structured info retrieval successful\"",
        "}"
        );
    }
}

