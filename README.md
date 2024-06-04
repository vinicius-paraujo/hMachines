# hMachines [1.2.0]
Desenvolvido por: Markineo

Primeiramente, agradeço pela compra e pela paciência, qualquer eventual problema pode ser relatado no meu discord: @markineo.

## Requerimentos
O plugin possui algumas dependências. Aqui está a lista:
- `PlaceholderAPI`
- `Vault`
- `LuckPerms`
- `HeadDatabase`
- `WorldGuard`

## Sobre
### Combustível
- Cada combustível é definido no arquivo '`combustiveis.yml`', lá você define as informações dele, é importante ressaltar que o plugin entende que um litro é equivalente a um drop.
- Existem dois tipos de combustiveis: `bruto` e `refinado`. Para refinar eles, é necessário fazer uso de uma Refinaria.
- Você pode definir o percentual de chance de cada combustível bruto quebrar uma máquina.

### Refinaria
- A refinaria é adquirida na loja, ela serve basicamente ..para refinar. (risos em programador)

### Máquinas
- Os drops das máquinas podem ser definidos basicamente de duas maneiras: `por arquivo` e pelo comando `/madmin adicionardrop <id da máquina> <porcentagem%>`. Para o comando funcionar, você deve estar segurando o item que deseja adicionar.
- Para remover um drop, você pode usar `/madmin removerdrop <id da máquina> <id do drop>`.

### Menus
- Os menus possuem funcionalidades interessantes, inicialmente, existem apenas dois: `combustiveis.yml` e `maquinas.yml`. Você pode adicionar mais, porém eles vão precisar de uma ID e um TITLE diferente. Você pode configurar diferente ações para o clique.
- `left_action` e `right_action` suportam as seguintes funções
```yml
- "compra <produto:id>" -> Os produtos aceitos são: 'maquina/refinaria/combustivel/fix'
- "playermessage <mensagem>"
- "playercmd <comando>"
- "consolecmd <comando>"
```

Exemplo:
```yml
itens:
    1:
        type: "icon"
        id: '397:3'
        skull_owner: '{heads-9356}'
        name: "&3Cancelar"
        left_action: "playermessage &7Você cancelou a ação."
        description:
        - "&7Clique para cancelar."
        pos_x: 0
        pos_y: 0
```

### HeadsDatabase
- O plugin é compatível com o HeadsDatabase, então você pode adicionar Heads customizadas, basta inserir `{heads-ID}` ao skull_owner.

## Permissões
- hmachines.admin
- hmachines.stack
- hmachines.use
