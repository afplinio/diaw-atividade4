package br.com.pokedex.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.pokedex.model.Pokemon;

public interface PokemonRepository extends MongoRepository<Pokemon, Integer> {

}
