package br.com.pokedex.seed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê os comandos INSERT INTO do Script_Pokedex.sql e devolve as linhas de cada tabela.
 * Não é um interpretador SQL completo: entende apenas o formato usado no script
 * (INSERT INTO Tabela (`col1`, `col2`) VALUES (...), (...);).
 */
public final class SqlScriptParser {

	private static final Pattern INSERT = Pattern.compile(
			"INSERT\\s+INTO\\s+`?(\\w+)`?\\s*\\(([^)]*)\\)\\s*VALUES\\s*(.*?);",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private SqlScriptParser() {
	}

	/**
	 * @return mapa "nome da tabela" -> linhas, em que cada linha é um mapa "coluna" -> valor.
	 *         Valores NULL viram null; textos vêm sem as aspas.
	 */
	public static Map<String, List<Map<String, String>>> parse(String script) {
		Map<String, List<Map<String, String>>> tables = new LinkedHashMap<>();
		Matcher matcher = INSERT.matcher(script);

		while (matcher.find()) {
			String table = matcher.group(1);
			List<String> columns = Arrays.stream(matcher.group(2).split(","))
					.map(column -> column.replace("`", "").trim())
					.toList();

			List<Map<String, String>> rows = tables.computeIfAbsent(table, key -> new ArrayList<>());
			for (List<String> values : parseTuples(matcher.group(3))) {
				if (values.size() != columns.size()) {
					throw new IllegalArgumentException("Linha com quantidade de colunas inválida na tabela "
							+ table + ": " + values);
				}
				Map<String, String> row = new LinkedHashMap<>();
				for (int i = 0; i < columns.size(); i++) {
					row.put(columns.get(i), values.get(i));
				}
				rows.add(row);
			}
		}
		return tables;
	}

	/** Separa "(1, 'Ash', 9), (2, 'Brock', 1)" em listas de valores, respeitando textos entre aspas. */
	private static List<List<String>> parseTuples(String values) {
		List<List<String>> tuples = new ArrayList<>();
		List<String> current = null;
		StringBuilder token = new StringBuilder();
		boolean inString = false;
		boolean quoted = false;

		for (int i = 0; i < values.length(); i++) {
			char c = values.charAt(i);

			if (inString) {
				if (c == '\'' && i + 1 < values.length() && values.charAt(i + 1) == '\'') {
					token.append('\''); // '' dentro do texto é uma aspa escapada (ex.: Farfetch''d)
					i++;
				} else if (c == '\'') {
					inString = false;
				} else {
					token.append(c);
				}
			} else if (c == '\'') {
				token.setLength(0); // descarta espaços antes da aspa
				inString = true;
				quoted = true;
			} else if (c == '(' && current == null) {
				current = new ArrayList<>();
			} else if ((c == ',' || c == ')') && current != null) {
				current.add(toValue(token.toString(), quoted));
				token.setLength(0);
				quoted = false;
				if (c == ')') {
					tuples.add(current);
					current = null;
				}
			} else if (current != null && !quoted) {
				token.append(c);
			}
		}
		return tuples;
	}

	private static String toValue(String raw, boolean quoted) {
		if (quoted) {
			return raw;
		}
		String value = raw.trim();
		return value.equalsIgnoreCase("NULL") ? null : value;
	}

}
