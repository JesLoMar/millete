package com.puntomartinez.millete.investments.infrastructure.out.marketdata;

/** Safe-to-display provider failure; API credentials and response bodies are never included. */
public final class MarketDataProviderException extends RuntimeException {
    public MarketDataProviderException(String message) { super(message); }
    public MarketDataProviderException(String message, Throwable cause) { super(message, cause); }
}
