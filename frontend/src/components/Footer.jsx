import '../css/Footer.css'

function Footer() {
  return (
    <footer className="footer">
      <p>
        Pokédex desenvolvida com React + Vite, Spring Boot e MongoDB · Dados:{' '}
        <code>Script_Pokedex.sql</code> · Imagens:{' '}
        <a href="https://pokeapi.co/" target="_blank" rel="noreferrer">
          PokéAPI
        </a>
      </p>
      <p className="footer__note">
        Pokémon e seus nomes são marcas registradas de Nintendo, Game Freak e The Pokémon Company.
        Projeto acadêmico sem fins comerciais.
      </p>
    </footer>
  )
}

export default Footer
