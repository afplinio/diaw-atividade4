package br.com.pokedex.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.pokedex.model.Trainer;

public interface TrainerRepository extends MongoRepository<Trainer, Integer> {

}
