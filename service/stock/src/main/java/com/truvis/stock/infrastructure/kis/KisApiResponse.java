package com.truvis.stock.infrastructure.kis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 한국투자증권 API 응답 구조
 * - 국내주식 현재가 조회 API 응답
 */
@Getter
@NoArgsConstructor
@ToString
public class KisApiResponse {

    /**
     * 응답 코드
     * - "0": 정상
     * - 기타: 오류
     */
    @JsonProperty("rt_cd")
    private String rtCd;

    /**
     * 응답 메시지
     */
    @JsonProperty("msg1")
    private String msg1;

    /**
     * 응답 데이터
     */
    @JsonProperty("output")
    private Output output;

    @Getter
    @NoArgsConstructor
    @ToString
    public static class Output {

        /**
         * 현재가 (주식 가격)
         */
        @JsonProperty("stck_prpr")
        private String stckPrpr;

        /**
         * 전일 대비
         */
        @JsonProperty("prdy_vrss")
        private String prdyVrss;

        /**
         * 전일 대비율
         */
        @JsonProperty("prdy_ctrt")
        private String prdyCtrt;

        /**
         * 고가
         */
        @JsonProperty("stck_hgpr")
        private String stckHgpr;

        /**
         * 저가
         */
        @JsonProperty("stck_lwpr")
        private String stckLwpr;

        /**
         * 시가
         */
        @JsonProperty("stck_oprc")
        private String stckOprc;

        /**
         * 거래량
         */
        @JsonProperty("acml_vol")
        private String acmlVol;
    }
}