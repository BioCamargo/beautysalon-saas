# 🛡️ NEXI PRO SaaS — Manual de Backups & Disaster Recovery

Este diretório contém os scripts automatizados de backup lógico e restauração de emergência para o banco de dados PostgreSQL 16.

**Custo Total: R$ 0,00 (Totalmente Gratuito)**

---

## 📁 Arquivos Disponíveis

| Arquivo | Ambiente | Finalidade |
| :--- | :--- | :--- |
| `backup_postgres.ps1` | Windows / PowerShell | Extrai dump compactado (`.dump`), gera SHA-256 e expurga arquivos com mais de 14 dias. |
| `backup_postgres.sh` | Linux / Docker / Cloud | Idem para servidores Linux ou containers de produção. |
| `restore_postgres.ps1` | Windows / PowerShell | Restauração de desastre com validação de integridade. |
| `restore_postgres.sh` | Linux / Bash | Restauração de desastre no Linux. |

---

## 🚀 Como Executar o Backup Manualmente

### No Windows (PowerShell):
```powershell
# Execução básica (usa variáveis de ambiente ou padrões localhost)
.\scripts\backup\backup_postgres.ps1

# Ou informando parâmetros específicos do banco:
.\scripts\backup\backup_postgres.ps1 -DbHost "localhost" -DbPort "5432" -DbName "beautysalon_db" -DbUser "postgres"
```

### No Linux:
```bash
chmod +x scripts/backup/*.sh
./scripts/backup/backup_postgres.sh
```

---

## ⏰ Como Agendar para Rodar Todo Dia Automaticamente (Grátis)

### No Windows (Agendador de Tarefas / Task Scheduler):
1. Abra o **Agendador de Tarefas** do Windows (`taskschd.msc`).
2. Clique em **Criar Tarefa Básica**.
3. Nome: `NEXI_PRO_Postgres_Backup_Diario`.
4. Disparador: **Diariamente às 03:00**.
5. Ação: **Iniciar um programa**:
   - Programa: `powershell.exe`
   - Argumentos: `-ExecutionPolicy Bypass -File "C:\Dev\Projetos\Proj_Studio\scripts\backup\backup_postgres.ps1"`
6. Concluir. O backup rodará todo dia de madrugada sem gastar nada.

### No Linux (Crontab):
```bash
# Rodar todo dia às 03:00 da manhã
0 3 * * * /caminho/do/projeto/scripts/backup/backup_postgres.sh >> /var/log/cron_backup.log 2>&1
```

---

## 🔄 Como Restaurar em Caso de Desastre (Disaster Recovery)

Se o banco sofrer qualquer corrupção ou perda:
```powershell
.\scripts\backup\restore_postgres.ps1 -DumpFile ".\backups\backup_beautysalon_db_20261027_030000.dump"
```
