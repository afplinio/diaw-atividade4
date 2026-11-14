package br.com.pokedex.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.pokedex.model.Pokemon;
import br.com.pokedex.service.PokemonService;

@RestController
@RequestMapping("/api/pokemons")
public class PokemonController {

	private final PokemonService pokemonService;

	public PokemonController(PokemonService pokemonService) {
		this.pokemonService = pokemonService;
	}

	/** GET /api/pokemons - lista todos os Pokémon. */
	@GetMapping
	public List<Pokemon> findAll() {
		return pokemonService.findAll();
	}

	/** GET /api/pokemons/{id} - busca um Pokémon pelo ID. */
	@GetMapping("/{id}")
	public Pokemon findById(@PathVariable Integer id) {
		return pokemonService.findById(id);
	}

	/** POST /api/pokemons - cadastra um Pokémon (201 Created). */
	@PostMapping
	public ResponseEntity<Pokemon> create(@RequestBody Pokemon pokemon) {
		Pokemon created = pokemonService.create(pokemon);
		return ResponseEntity.created(URI.create("/api/pokemons/" + created.getId())).body(created);
	}

	/** PUT /api/pokemons/{id} - atualiza um Pokémon. */
	@PutMapping("/{id}")
	public Pokemon update(@PathVariable Integer id, @RequestBody Pokemon pokemon) {
		return pokemonService.update(id, pokemon);
	}

	/** DELETE /api/pokemons/{id} - remove um Pokémon (204 No Content). */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		pokemonService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
