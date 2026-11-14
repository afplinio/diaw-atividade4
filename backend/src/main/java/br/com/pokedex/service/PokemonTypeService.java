package br.com.pokedex.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import br.com.pokedex.model.PokemonType;
import br.com.pokedex.repository.PokemonTypeRepository;

@Service
public class PokemonTypeService {

	private final PokemonTypeRepository typeRepository;

	public PokemonTypeService(PokemonTypeRepository typeRepository) {
		this.typeRepository = typeRepository;
	}

	public List<PokemonType> findAll() {
		return typeRepository.findAll(Sort.by("id"));
	}

}
