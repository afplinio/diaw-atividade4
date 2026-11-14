package br.com.pokedex.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.pokedex.model.Trainer;

public interface TrainerRepository extends MongoRepository<Trainer, Integer> {

	/** Treinadores que têm o Pokémon informado no time. */
	List<Trainer> findByPokemonIdsContaining(Integer pokemonId);

}
