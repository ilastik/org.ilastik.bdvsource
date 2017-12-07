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
    public void testStructuredInfoDeserialization() throws Exception {
        String jsonText = "{ \"test_int\" : 4}";
        ObjectMapper objectMapper = new ObjectMapper();
        StructuredInfo loadedObject = objectMapper.readValue(jsonText, StructuredInfo.class);
        assertEquals(loadedObject.test_int, 4);
    }
}

