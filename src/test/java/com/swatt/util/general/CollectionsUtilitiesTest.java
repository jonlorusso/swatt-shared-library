package com.swatt.util.general;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CollectionsUtilitiesTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNewHashMap() throws Exception {
        String key1 = "key1";
        String value1 = "value1";
        String key2 = "key2";
        String value2 = "value2";
        
        Map<String, String> map = CollectionsUtilities.newHashMap(key1, value1);
        
        assertThat(map.get(key1), is(value1));
        
        map = CollectionsUtilities.newHashMap(key1, value1, key2, value2);
        
        assertThat(map.get(key1), is(value1));
        assertThat(map.get(key2), is(value2));
        
        map = CollectionsUtilities.newHashMap(new String[] { key1, key2 }, new String[] { value1, value2 });
        
        assertThat(map.get(key1), is(value1));
        assertThat(map.get(key2), is(value2));
    }
}
