package br.com.pokedex.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class SqlScriptParserTest {

	@Test
	void lerTextosComEspacosEAspasEscapadas() {
		String sql = """
				INSERT INTO Pokemon
				    (`ID_Pokemon`, `Pokemon`, `HP`)
				VALUES
				    (83, 'Farfetch''d', 52),
				    (122, 'Mr. Mime', NULL)
				;
				""";

		List<Map<String, String>> rows = SqlScriptParser.parse(sql).get("Pokemon");

		assertThat(rows).hasSize(2);
		assertThat(rows.get(0)).containsEntry("ID_Pokemon", "83").containsEntry("Pokemon", "Farfetch'd");
		assertThat(rows.get(1)).containsEntry("Pokemon", "Mr. Mime").containsEntry("HP", null);
	}

	@Test
	void lerTodasAsTabelasDoScriptDaAtividade() throws IOException {
		String script = new ClassPathResource("Script_Pokedex.sql").getContentAsString(StandardCharsets.UTF_8);

		Map<String, List<Map<String, String>>> tables = SqlScriptParser.parse(script);

		assertThat(tables).containsKeys("Pokemon", "Types", "Trainers", "Rel_Pokemon_Type", "Rel_Trainer_Pokemon");
		assertThat(tables.get("Pokemon")).hasSize(151);
		assertThat(tables.get("Types")).hasSize(18);
		assertThat(tables.get("Trainers")).hasSize(12);
		assertThat(tables.get("Pokemon").get(24))
				.containsEntry("Pokemon", "Pikachu")
				.containsEntry("Speed", "90");
	}

}
