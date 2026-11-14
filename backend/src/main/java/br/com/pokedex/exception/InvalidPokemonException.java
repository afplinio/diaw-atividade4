package br.com.pokedex.exception;

/** Dados do Pokémon válidos no formato, mas inconsistentes com o banco (HTTP 400). */
public class InvalidPokemonException extends RuntimeException {

	public InvalidPokemonException(String message) {
		super(message);
	}

}
