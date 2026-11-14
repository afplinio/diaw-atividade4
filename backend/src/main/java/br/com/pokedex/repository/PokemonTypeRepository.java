package br.com.pokedex.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.pokedex.model.PokemonType;

public interface PokemonTypeRepository extends MongoRepository<PokemonType, Integer> {

}
