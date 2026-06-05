package com.dace.service;

import com.dace.common.BizException;
import com.dace.common.ErrorCode;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WechatService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${dace.wechat.app-id:}")
    private String appId;

    @Value("${dace.wechat.app-secret:}")
    private String appSecret;

    public String codeToOpenid(String code) {
        if (isPlaceholder(appId) || isPlaceholder(appSecret)) {
            return "dev-" + code;
        }
        String url = UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
            .queryParam("appid", appId)
            .queryParam("secret", appSecret)
            .queryParam("js_code", code)
            .queryParam("grant_type", "authorization_code")
            .toUriString();
        Map<?, ?> response = restTemplate.getForObject(url, Map.class);
        if (response == null || response.get("openid") == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return response.get("openid").toString();
    }

    private boolean isPlaceholder(String value) {
        return value == null || value.isBlank() || value.startsWith("replace-with");
    }
}
