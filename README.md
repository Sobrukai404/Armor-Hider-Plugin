# 🛡️ ArmorHiderPlugin

Um plugin para servidores Spigot que permite aos jogadores **esconder ou mostrar sua armadura** visualmente para outros jogadores, mantendo sua funcionalidade. Utiliza a [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) para interceptar pacotes e manipular a visualização da armadura.

---

## 📦 Requisitos

- **Servidor Minecraft:** Spigot ou Paper 1.19+
- **Dependência:** [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)

---

## ⚙️ Instalação

1. Baixe o arquivo `.jar` compilado do plugin.
2. Coloque o `.jar` na pasta `plugins/` do seu servidor.
3. Certifique-se de que o plugin [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) está instalado.
4. Reinicie ou recarregue o servidor.

---

## 🧪 Comandos

| Comando | Descrição |
|--------|-----------|
| `/esconder <parte>` | Esconde partes da armadura. Partes possíveis: `capacete`, `peitoral`, `calça`, `bota`, `tudo` |
| `/mostrar` | Torna toda a armadura visível novamente |

---

## 👤 Como Funciona

- Quando um jogador usa o comando `/esconder`, a armadura especificada é escondida **apenas visualmente** dos outros jogadores.
- O jogador ainda permanece com a armadura equipada e mantém os efeitos de proteção normalmente.
- A manipulação é feita interceptando pacotes do tipo `ENTITY_EQUIPMENT` usando o ProtocolLib.
- A armadura escondida é substituída por `AIR` (vazio) apenas na visão dos outros jogadores.

---

## 📚 Estrutura do Código

### `onEnable()`
- Registra um **PacketListener** que modifica os pacotes enviados aos jogadores para ocultar partes da armadura.
- Registra **eventos Bukkit** para reagir a interações e danos, forçando a atualização da armadura.
- Registra os comandos `/esconder` e `/mostrar`.

### `refresh(Player p)`
- Força a atualização do visual da armadura para todos os jogadores online.
- Envia pacotes de alteração de equipamento (`sendEquipmentChange`) com base na armadura oculta.

### `removeArmorAttributes(Player player)`
- Remove os atributos (`AttributeModifier`) dos itens de armadura.
- Essa função está definida, mas não é utilizada diretamente. Pode ser útil para evitar efeitos duplicados ou bugs visuais.

---

## 📌 Possíveis Melhorias Futuras

- Suporte a configurações persistentes (armazenar preferências entre reinícios).
- Suporte a esconder armaduras de outros jogadores (ex: `/esconder jogador parte`).
- Adição de permissões Bukkit (ex: `armorhider.hide`).
- GUI para facilitar a escolha de armadura a esconder.

---

## 🧑‍💻 Autor

Desenvolvido por [Sobrukai404](https://github.com/Sobrukai404)  
Este projeto é open-source. Contribuições são bem-vindas!

---

## 📜 Licença

Este projeto está licenciado sob a [MIT License](https://opensource.org/licenses/MIT).
