package br.com.pokedex.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento da coleção "trainers".
 * O time do treinador fica em "pokemonIds", substituindo a tabela Rel_Trainer_Pokemon.
 * A lista pode repetir um mesmo Pokémon (ex.: dois Pidgey no time).
 */
@Document(collection = "trainers")
public class Trainer {

	@Id
	private Integer id;

	private String name;

	private Integer badges;

	private List<Integer> pokemonIds = new ArrayList<>();

	public Trainer() {
	}

	public Trainer(Integer id, String name, Integer badges, List<Integer> pokemonIds) {
		this.id = id;
		this.name = name;
		this.badges = badges;
		setPokemonIds(pokemonIds);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getBadges() {
		return badges;
	}

	public void setBadges(Integer badges) {
		this.badges = badges;
	}

	public List<Integer> getPokemonIds() {
		return pokemonIds;
	}

	public void setPokemonIds(List<Integer> pokemonIds) {
		this.pokemonIds = pokemonIds == null ? new ArrayList<>() : new ArrayList<>(pokemonIds);
	}

}
