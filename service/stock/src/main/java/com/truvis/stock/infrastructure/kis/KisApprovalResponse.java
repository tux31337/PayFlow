package com.truvis.stock.infrastructure.kis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * KIS WebSocket 접속키 발급 응답
 */
@Data
public class KisApprovalResponse {
    
    @JsonProperty("approval_key")
    private String approvalKey;
}
