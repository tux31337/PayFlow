package com.truvis.stock.infrastructure.kis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * KIS WebSocket 실시간 체결가 응답
 * - TR_ID: H0STCNT0 (국내주식 실시간 체결가)
 */
@Data
public class KisWebSocketResponse {
    
    @JsonProperty("header")
    private Header header;
    
    @JsonProperty("body")
    private Body body;
    
    @Data
    public static class Header {
        @JsonProperty("tr_id")
        private String trId;
        
        @JsonProperty("tr_key")
        private String trKey;  // 종목코드
        
        @JsonProperty("encrypt")
        private String encrypt;
    }
    
    @Data
    public static class Body {
        @JsonProperty("rt_cd")
        private String rtCd;  // 응답 코드
        
        @JsonProperty("msg_cd")
        private String msgCd;
        
        @JsonProperty("msg1")
        private String msg1;
        
        @JsonProperty("output")
        private Output output;
    }
    
    @Data
    public static class Output {
        // 암호화 관련
        @JsonProperty("iv")
        private String iv;
        
        @JsonProperty("key")
        private String key;
    }
}
