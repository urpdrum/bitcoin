////package com.alerta.bitcoin.dto;
////
////
////import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
////import com.fasterxml.jackson.annotation.JsonProperty;
////import java.util.Map;
////
////    @JsonIgnoreProperties(ignoreUnknown = true)
////    public class BitcoinPriceResponse {
////
////        @JsonProperty("bitcoin")
////        private Map<String, Double> bitcoin;
////
////        public Map<String, Double> getBitcoin() {
////            return bitcoin;
////        }
////
////        public void setBitcoin(Map<String, Double> bitcoin) {
////            this.bitcoin = bitcoin;
////        }
////
////        public Double getPriceInCurrency(String currency) {
////            if (bitcoin != null && bitcoin.containsKey(currency.toLowerCase())) {
////                return bitcoin.get(currency.toLowerCase());
////            }
////            return null;
////        }
////    }
//
//
//    package com.alerta.bitcoin.dto;
//
// // Ou o seu pacote
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import java.util.Map;
//
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class BitcoinPriceResponse {
//
//    @JsonProperty("bitcoin") // Este é o nome da chave principal no JSON
//    private Map<String, Double> bitcoin; // O valor desta chave é um outro mapa
//
//    public Map<String, Double> getBitcoin() {
//        return bitcoin;
//    }
//
//    public void setBitcoin(Map<String, Double> bitcoin) {
//        this.bitcoin = bitcoin;
//    }
//
//    // Método auxiliar para pegar o preço em uma moeda específica diretamente
//    public Double getPriceInCurrency(String currency) {
//        if (bitcoin != null && bitcoin.containsKey(currency.toLowerCase())) {
//            return bitcoin.get(currency.toLowerCase());
//        }
//        return null;
//    }
//}
