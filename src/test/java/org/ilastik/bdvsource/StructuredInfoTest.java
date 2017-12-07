/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ilastik.bdvsource;

import com.google.gson.Gson;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dkutra
 */
public class StructuredInfoTest {

    public StructuredInfoTest() {
    }

    @Test
    public void testStructuredImageInfoDeserialization() throws Exception {
        String jsonString = String.join("",
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
        "}"
        );
        StructuredInfo.ImageInfo loadedObject;
        Gson gson = new Gson();
        loadedObject = gson.fromJson(jsonString, StructuredInfo.ImageInfo.class);
        assertEquals(loadedObject.axes, "tczyx");
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
                  "2,",
                  "3,",
                  "450,",
                  "550",
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
        Gson gson = new Gson();
        StructuredInfo loadedObject;
        loadedObject = gson.fromJson(jsonString, StructuredInfo.class);
        assertEquals(loadedObject.message, "Structured info retrieval successful");
        assertEquals(loadedObject.image_names[0], "testimg.1");
        StructuredInfo.ImageInfo info0 = loadedObject.states[0][0];
        assertArrayEquals(info0.shape, new int[] {1, 2, 3, 450, 550});
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
                  "2,",
                  "3,",
                  "450,",
                  "550",
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
                  "111,",
                  "222,",
                  "333,",
                  "444,",
                  "555",
                "],",
                "\"dtype\": \"float32\",",
                "\"version\": 0,",
                "\"lane_number\": 1,",
                "\"dataset_name\": \"testimg.2\",",
                "\"source_name\": \"CachedPredictionImage\"",
              "}",
            "]",
          "],",
          "\"image_names\": [",
            "\"testimg.1\",",
            "\"testimg.2\"",
          "],",
          "\"message\": \"Structured info retrieval successful\"",
        "}"
        );
        Gson gson = new Gson();
        StructuredInfo loadedObject;
        loadedObject = gson.fromJson(jsonString, StructuredInfo.class);
        assertEquals(loadedObject.message, "Structured info retrieval successful");
        assertEquals(loadedObject.image_names[0], "testimg.1");
        StructuredInfo.ImageInfo info00 = loadedObject.states[0][0];
        assertArrayEquals(info00.shape, new int[] {1, 2, 3, 450, 550});
        StructuredInfo.ImageInfo info11 = loadedObject.states[1][1];
        assertArrayEquals(info11.shape, new int[] {111, 222, 333, 444, 555});
    }
}

