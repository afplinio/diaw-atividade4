package br.com.pokedex.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.pokedex.model.Pokemon;
import br.com.pokedex.model.Trainer;
import br.com.pokedex.service.TrainerService;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

	private final TrainerService trainerService;

	public TrainerController(TrainerService trainerService) {
		this.trainerService = trainerService;
	}

	/** GET /api/trainers - lista os treinadores com os IDs dos Pokémon do time. */
	@GetMapping
	public List<Trainer> findAll() {
		return trainerService.findAll();
	}

	/** GET /api/trainers/{id} - busca um treinador pelo ID. */
	@GetMapping("/{id}")
	public Trainer findById(@PathVariable Integer id) {
		return trainerService.findById(id);
	}

	/** GET /api/trainers/{id}/pokemons - Pokémon do time do treinador. */
	@GetMapping("/{id}/pokemons")
	public List<Pokemon> findTeam(@PathVariable Integer id) {
		return trainerService.findTeam(id);
	}

}
