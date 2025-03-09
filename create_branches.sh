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
    git remote set-url origin git@github.com:$(git remote get-url origin | sed -E 's/https:\/\/github.com\//github.com:/')
    echo "URL remota atualizada para SSH."
fi

# Sincroniza o repositório local com o master (ou main) remoto
git checkout main 2>/dev/null || git checkout master
if [ $? -ne 0 ]; then
    echo "Erro: Não foi possível mudar para o branch master ou main."
    exit 1
fi

git pull origin $(git symbolic-ref --short HEAD)
if [ $? -ne 0 ]; then
    echo "Erro: Não foi possível sincronizar com o remoto."
    exit 1
fi

# Função para criar branch se não existir e sincronizar com master
create_and_sync_branch() {
    local branch_name=$1
    if git show-ref --verify --quiet refs/heads/$branch_name; then
        echo "Branch $branch_name já existe, sincronizando com master."
        git checkout $branch_name
        git merge main 2>/dev/null || git merge master  # Tenta primeiro com main e depois com master
        if [ $? -ne 0 ]; then
            echo "Erro ao fazer merge na branch $branch_name. Resolve conflitos, se houver."
            exit 1
        fi
        git push origin $branch_name
        echo "Branch $branch_name sincronizada com master e enviada para o remoto."
    else
        echo "Branch $branch_name não existe, criando e enviando para o remoto."
        git checkout main 2>/dev/null || git checkout master
        git checkout -b $branch_name
        if [ $? -ne 0 ]; then
            echo "Erro: Não foi possível criar a branch $branch_name."
            exit 1
        fi
        git push origin $branch_name
        echo "Branch $branch_name criada e enviada com sucesso."
    fi
}

# Cria e sincroniza as branches
create_and_sync_branch feature/gabriel
create_and_sync_branch feature/hugo
create_and_sync_branch feature/andre
create_and_sync_branch feature/elvis
create_and_sync_branch feature/mirian

echo "Todas as branches foram criadas ou sincronizadas com sucesso."
