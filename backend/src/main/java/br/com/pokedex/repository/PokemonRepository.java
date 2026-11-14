package br.com.pokedex.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.pokedex.model.Pokemon;

public interface PokemonRepository extends MongoRepository<Pokemon, Integer> {

	/** Pokémon com o maior ID, usado para gerar o próximo número no cadastro. */
	Optional<Pokemon> findTopByOrderByIdDesc();

}
