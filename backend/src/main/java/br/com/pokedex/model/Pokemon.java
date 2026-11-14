package br.com.pokedex.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento da coleção "pokemons".
 * Os tipos ficam embutidos no próprio documento (ex.: ["Grass", "Poison"]),
 * substituindo a tabela de relacionamento Rel_Pokemon_Type do script SQL.
 */
@Document(collection = "pokemons")
public class Pokemon {

	@Id
	@Min(value = 1, message = "O número deve ser maior que zero.")
	@Max(value = 9999, message = "O número deve ser no máximo 9999.")
	private Integer id;

	@NotBlank(message = "O nome é obrigatório.")
	@Size(max = 30, message = "O nome deve ter no máximo 30 caracteres.")
	private String name;

	@NotNull(message = "O campo HP é obrigatório.")
	@Min(value = 1, message = "O campo HP deve ser no mínimo 1.")
	@Max(value = 255, message = "O campo HP deve ser no máximo 255.")
	private Integer hp;

	@NotNull(message = "O campo Ataque é obrigatório.")
	@Min(value = 1, message = "O campo Ataque deve ser no mínimo 1.")
	@Max(value = 255, message = "O campo Ataque deve ser no máximo 255.")
	private Integer attack;

	@NotNull(message = "O campo Defesa é obrigatório.")
	@Min(value = 1, message = "O campo Defesa deve ser no mínimo 1.")
	@Max(value = 255, message = "O campo Defesa deve ser no máximo 255.")
	private Integer defense;

	@NotNull(message = "O campo Ataque Especial é obrigatório.")
	@Min(value = 1, message = "O campo Ataque Especial deve ser no mínimo 1.")
	@Max(value = 255, message = "O campo Ataque Especial deve ser no máximo 255.")
	private Integer specialAttack;

	@NotNull(message = "O campo Defesa Especial é obrigatório.")
	@Min(value = 1, message = "O campo Defesa Especial deve ser no mínimo 1.")
	@Max(value = 255, message = "O campo Defesa Especial deve ser no máximo 255.")
	private Integer specialDefense;

	@NotNull(message = "O campo Velocidade é obrigatório.")
	@Min(value = 1, message = "O campo Velocidade deve ser no mínimo 1.")
	@Max(value = 255, message = "O campo Velocidade deve ser no máximo 255.")
	private Integer speed;

	@Size(max = 2, message = "Um Pokémon pode ter no máximo 2 tipos.")
	private List<@NotBlank(message = "O tipo não pode ser vazio.") String> types = new ArrayList<>();

	public Pokemon() {
	}

	public Pokemon(Integer id, String name, Integer hp, Integer attack, Integer defense,
			Integer specialAttack, Integer specialDefense, Integer speed, List<String> types) {
		this.id = id;
		this.name = name;
		this.hp = hp;
		this.attack = attack;
		this.defense = defense;
		this.specialAttack = specialAttack;
		this.specialDefense = specialDefense;
		this.speed = speed;
		setTypes(types);
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

	public Integer getHp() {
		return hp;
	}

	public void setHp(Integer hp) {
		this.hp = hp;
	}

	public Integer getAttack() {
		return attack;
	}

	public void setAttack(Integer attack) {
		this.attack = attack;
	}

	public Integer getDefense() {
		return defense;
	}

	public void setDefense(Integer defense) {
		this.defense = defense;
	}

	public Integer getSpecialAttack() {
		return specialAttack;
	}

	public void setSpecialAttack(Integer specialAttack) {
		this.specialAttack = specialAttack;
	}

	public Integer getSpecialDefense() {
		return specialDefense;
	}

	public void setSpecialDefense(Integer specialDefense) {
		this.specialDefense = specialDefense;
	}

	public Integer getSpeed() {
		return speed;
	}

	public void setSpeed(Integer speed) {
		this.speed = speed;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types == null ? new ArrayList<>() : new ArrayList<>(types);
	}

}
