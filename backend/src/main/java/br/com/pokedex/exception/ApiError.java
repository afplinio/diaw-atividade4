package br.com.pokedex.exception;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Corpo padrão das respostas de erro da API. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(Instant timestamp, int status, String error, String message, Map<String, String> fields) {

	public ApiError(int status, String error, String message) {
		this(Instant.now(), status, error, message, Map.of());
	}

}
