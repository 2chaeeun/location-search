package com.chaeeun.locationsearch.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AddressUtils {
    private final Map<String, String> naverToKaKaoMap = new HashMap<>() {{
        put("서울특별시", "서울");
        put("부산광역시", "부산");
        put("대구광역시", "대구");
        put("인천광역시", "인천");
        put("광주광역시", "광주");
        put("대전광역시", "울산");
        put("경기도", "경기");
        put("강원도", "강원");
        put("충청북도", "충북");
        put("충청남도", "충남");
        put("전라북도", "전북");
        put("전라남도", "전남");
        put("경상북도", "경북");
        put("경상남도", "경남");
    }};

    public String abbreviateCityName(String addressName) {
        String result = "";
        String[] words = addressName.split(" ");
        words[0] = naverToKaKaoMap.getOrDefault(words[0], words[0]);
        result = String.join(" ", words);
        return result;
    }

    public String removeTag(String str) {
        return str.replaceAll("</?b>", "");
    }
}
