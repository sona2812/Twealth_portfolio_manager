package com.demo.service;

import com.demo.dto.StockDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class StockApiService {

    private final RestTemplate restTemplate;
    private final String defaultApiKey;
    private final double usdInrRate;
    
    // Popular stock symbols to fetch (limited to avoid rate limiting)
    private static final List<String> POPULAR_STOCKS = Arrays.asList(
        "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NVDA", "JPM", "V", "JNJ",
        "WMT", "PG", "MA", "UNH", "HD"
    );

    public StockApiService(
            @Value("${finnhub.api.key:}") String defaultApiKey,
            @Value("${usd.inr.rate:83.5}") double usdInrRate
    ) {
        this.restTemplate = new RestTemplate();
        this.defaultApiKey = defaultApiKey;
        this.usdInrRate = usdInrRate;
    }
    
    /**
     * Gets the API key to use, preferring the provided one over the default from properties
     */
    private String getApiKey(String providedApiKey) {
        if (providedApiKey != null && !providedApiKey.isEmpty()) {
            return providedApiKey;
        }
        return defaultApiKey != null && !defaultApiKey.isEmpty() ? defaultApiKey : null;
    }

    /**
     * Fetches live stock quote from Finnhub API
     * @param symbol Stock symbol
     * @param apiKey API key (optional, will use default from properties if not provided)
     */
    public StockDTO fetchStockQuote(String symbol, String apiKey) {
        String keyToUse = getApiKey(apiKey);
        if (keyToUse == null) {
            System.err.println("No API key provided for fetching stock quote");
            return null;
        }
        try {
            String url = String.format(
                "https://finnhub.io/api/v1/quote?symbol=%s&token=%s",
                symbol, keyToUse
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null || response.isEmpty()) {
                return null;
            }
            
            // Finnhub returns: {"c": currentPrice, "d": change, "dp": changePercent, "h": high, "l": low, "o": open, "pc": previousClose, "t": timestamp}
            Object currentPriceObj = response.get("c");
            Object changePercentObj = response.get("dp");
            
            if (currentPriceObj == null) {
                return null;
            }
            
            Double price = 0.0;
            if (currentPriceObj instanceof Number) {
                price = ((Number) currentPriceObj).doubleValue();
            } else if (currentPriceObj instanceof String) {
                price = Double.parseDouble((String) currentPriceObj);
            }
            
            if (price <= 0) {
                return null;
            }

            // Convert USD price to INR using configured rate
            Double priceInInr = price * usdInrRate;
            
            Double changePercent = 0.0;
            if (changePercentObj != null) {
                if (changePercentObj instanceof Number) {
                    changePercent = ((Number) changePercentObj).doubleValue();
                } else if (changePercentObj instanceof String) {
                    changePercent = Double.parseDouble((String) changePercentObj);
                }
            } else if (price > 0) {
                // Calculate change percent if not provided
                Object previousCloseObj = response.get("pc");
                if (previousCloseObj != null) {
                    Double previousClose = 0.0;
                    if (previousCloseObj instanceof Number) {
                        previousClose = ((Number) previousCloseObj).doubleValue();
                    } else if (previousCloseObj instanceof String) {
                        previousClose = Double.parseDouble((String) previousCloseObj);
                    }
                    if (previousClose > 0) {
                        changePercent = ((price - previousClose) / previousClose) * 100;
                    }
                }
            }
            
            // Try to get company name from profile
            String fullCompanyName = fetchCompanyName(symbol, keyToUse);
            if (fullCompanyName == null || fullCompanyName.isEmpty()) {
                fullCompanyName = symbol;
            }
            
            // Generate a temporary ID based on symbol hash for API-only stocks
            Long tempId = (long) Math.abs(symbol.hashCode());
            
            // Note: we now return price in INR so frontend sees values already converted
            return new StockDTO(tempId, symbol, fullCompanyName, priceInInr, 0, 0.0, changePercent);
            
        } catch (Exception e) {
            System.err.println("Error fetching stock quote for " + symbol + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetches company name from Finnhub Profile API
     */
    private String fetchCompanyName(String symbol, String apiKey) {
        try {
            String url = String.format(
                "https://finnhub.io/api/v1/stock/profile2?symbol=%s&token=%s",
                symbol, apiKey
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("name")) {
                return (String) response.get("name");
            }
        } catch (Exception e) {
            // Silently fail - we'll use symbol as fallback
        }
        return null;
    }

    /**
     * Fetches all popular stocks with live prices
     * @param apiKey API key (optional, will use default from properties if not provided)
     */
    public List<StockDTO> fetchAllPopularStocks(String apiKey) {
        String keyToUse = getApiKey(apiKey);
        if (keyToUse == null) {
            System.err.println("No API key provided for fetching popular stocks");
            return new ArrayList<>();
        }
        
        List<StockDTO> stocks = new ArrayList<>();
        
        for (String symbol : POPULAR_STOCKS) {
            StockDTO stock = fetchStockQuote(symbol, keyToUse);
            if (stock != null) {
                stocks.add(stock);
            }
            // Add small delay to avoid rate limiting (Finnhub free tier: 60 calls/min)
            try {
                Thread.sleep(1100); // 1.1 seconds between calls to stay under 60/min limit
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        return stocks;
    }

    /**
     * Fetches multiple stocks by symbols
     * @param symbols List of stock symbols
     * @param apiKey API key (optional, will use default from properties if not provided)
     */
    public List<StockDTO> fetchStocksBySymbols(List<String> symbols, String apiKey) {
        String keyToUse = getApiKey(apiKey);
        if (keyToUse == null) {
            System.err.println("No API key provided for fetching stocks");
            return new ArrayList<>();
        }
        
        List<StockDTO> stocks = new ArrayList<>();
        
        for (String symbol : symbols) {
            StockDTO stock = fetchStockQuote(symbol, keyToUse);
            if (stock != null) {
                stocks.add(stock);
            }
            // Add delay to avoid rate limiting (Finnhub free tier: 60 calls/min)
            try {
                Thread.sleep(1100); // 1.1 seconds between calls
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        return stocks;
    }
}
