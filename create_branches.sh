#!/bin/bash

# Verifica se o diretório atual é um repositório Git
if ! git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
    echo "Erro: Este diretório não é um repositório Git. Navegue para o diretório do projeto TaskBeats."
    exit 1
fi

# Corrige a URL remota para SSH, se necessário
current_remote=$(git remote get-url origin)
if [[ $current_remote == https://github.com/* ]]; then
    echo "Corrigindo URL remota de HTTPS para SSH..."
    git remote set-url origin git@github.com:ga6rielferreira/TaskBeat.git
    echo "URL remota atualizada para SSH."
fi

# Sincroniza o repositório local com o master remoto
git checkout master
if [ $? -ne 0 ]; then
    echo "Erro: Não foi possível mudar para o branch master."
    exit 1
fi

git pull origin master
if [ $? -ne 0 ]; then
    echo "Erro: Não foi possível sincronizar com o remoto."
    exit 1
fi

# Função para criar branch se não existir
create_branch() {
    local branch_name=$1
    if git show-ref --verify --quiet refs/heads/$branch_name; then
        echo "Branch $branch_name já existe, pulando criação."
    else
        git checkout master
        git checkout -b $branch_name
        if [ $? -ne 0 ]; then
            echo "Erro: Não foi possível criar a branch $branch_name."
            exit 1
        fi
        git push origin $branch_name
        if [ $? -ne 0 ]; then
            echo "Erro: Não foi possível enviar a branch $branch_name para o remoto."
            exit 1
        fi
        echo "Branch $branch_name criada e enviada com sucesso."
    fi
}

# Cria as branches
create_branch feature/gabriel
create_branch feature/hugo
create_branch feature/andre
create_branch feature/elvis
create_branch feature/mirian

echo "Todas as branches foram criadas ou verificadas com sucesso: feature/gabriel, feature/hugo, feature/andre, feature/elvis, feature/mirian"