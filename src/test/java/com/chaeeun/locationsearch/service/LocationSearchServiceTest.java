package com.chaeeun.locationsearch.service;

import com.chaeeun.locationsearch.utils.AddressUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LocationSearchServiceTest {

    @Autowired
    LocationSearchService locationSearchService;

    @Autowired
    AddressUtils addressUtils;

    @Test
    void isSameAddressTest() {
        String address1 = "서울 중구 을지로 35";
        String address2 = "서울특별시 중구 을지로 35 하나은행";
        boolean result = locationSearchService.isSameAddressName(address1, address2);
        assertTrue(result);
    }

    @Test
    void removeTagTest() {
        String str = "<b>남산돈까스</b>전문점";
        String removeTagStr = addressUtils.removeTag(str);
        assertEquals(removeTagStr, "남산돈까스전문점");
    }

    @Test
    void isSameNameTest1() {
        String s1 = "남산돈가스";
        String s2 = "남산돈까스전문점";
    }

    @Test
    void isSameNameTest2() {
        String s1 = "영동족발 1호점";
        String s2 = "영동족발 3호점";
    }
}