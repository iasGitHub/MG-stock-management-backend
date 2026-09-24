# Déploiement Railway (FE + BE + PostgreSQL)

Guide pas-à-pas, interface web Railway. Ordre conseillé : **sauvegarde → BE →
domaine BE → FE → domaine FE → CORS**.

## 0. Sauvegarde de la base existante (AVANT tout)

La base est en production avec des données. Avant le premier redeploy (qui
exécutera Flyway `V2`) :

- Ouvre le service **PostgreSQL** du projet.
- Menu du service → **Snapshots** (ou *Generate Snapshot*) → crée un snapshot.
- En alternative : `pg_dump` depuis un shell Railway.

## 1. Service PostgreSQL

- S'il existe déjà : **le conserver tel quel** (les données restent).
- Sinon : *+ New → Database → PostgreSQL*.
- Vérifier que le service **BE** reçoit bien la variable `DATABASE_URL`
  (Railway l'injecte automatiquement quand la DB est ajoutée en
  *Dependency/Linked resource* du service BE).

## 2. Service Backend (BE)

- *+ New → GitHub repo* → `iasGitHub/MG-stock-management-backend`
  (branche `master`). Si le service existe déjà : *Settings → Source* →
  vérifier que ce repo/branche est bien branché, puis **Redeploy**.
- Build : Railway détecte le `Dockerfile` à la racine (multi-stage Maven →
  JRE 21). Aucun réglage supplémentaire.
- **Variables** (service BE) :

| Variable | Valeur | Remarque |
|---|---|---|
| `JWT_SECRET` | ≥ 32 bytes Base64 — génère sans partager : `node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"` | **obligatoire** : sans lui le BE refuse de démarrer (fail-fast) |
| `SEED_DATA` | `true` | comptes démo créés **seulement si la base est vide** |
| `CORS_ALLOWED_ORIGINS` | `https://<domaine-FE>` | à renseigner après création du domaine FE (étape 5), puis Redeploy |
| `DATABASE_URL` | injectée par le lien DB | format `postgres://...` : l'entrypoint la convertit automatiquement en `DB_URL` JDBC |
| `DB_URL` | *non requis* | si tu la renseignes quand même (format `jdbc:postgresql://...`), elle prend le pas sur la conversion |
| `PORT` | injectée par Railway | déjà lue par l'application |

- **Redeploy**, puis ouvre les **Logs**.

### Ce que doivent dire les logs BE (succès)

- Flyway : `Successfully baselined schema ... to version 1` (base historique,
  V1 ignorée) **ou** `Migrating schema ... to version 1` (base fraîche),
- puis : `Migrating schema ... to version "2"` (alignement idempotent),
- aucun `Schema-validation failed` (Hibernate `validate` OK),
- `Tomcat started on port <PORT>`,
- si base vide : `Default accounts created: admin/admin123, manager/manager123`.

### Erreurs classiques

| Log | Cause |
|---|---|
| échec JWT / « secret-key » | `JWT_SECRET` absent ou < 32 bytes |
| `Schema-validation: missing column ...` | rapporter tel quel (cas non couvert par V2) |
| `Migration V2 ... failed` | rapporter le détail SQL (aucun risque : Flyway est transactionnel, la base reste dans son état antérieur) |

## 3. Domaine public BE

- Service BE → **Settings → Networking → Generate Domain**
  (ou domaine custom). Noter `https://<domaine-BE>`.

## 4. Service Frontend (FE)

- *+ New → GitHub repo* → `iasGitHub/MG-stock-management-frontend`
  (branche `master`).
- Réglages du service :
  - **Build Command** : `npm ci && npm run build:deploy`
    (génère `dist/...` puis `env.js` avec `window.API_URL`).
  - **Start Command** : `npx serve -s dist/stock-management-frontend/browser -l $PORT`
    (`-s` = fallback SPA pour les routes Angular).
- **Variables** (service FE) :

| Variable | Valeur |
|---|---|
| `API_URL` | `https://<domaine-BE>/api` (utilisée **au build** pour écrire `env.js`) |

- **Domaine FE** : Settings → Networking → Generate Domain.

## 5. CORS

- `CORS_ALLOWED_ORIGINS` du BE = `https://<domaine-FE>` (un seul domaine,
  sans slash final) → **Redeploy** du BE.

## 6. Validation manuelle

1. Ouvrir `https://<domaine-FE>`.
2. Connexion : `admin` / `admin123` **si la base était vide** ; sinon les
   comptes de ta base existante (le seed ne crée rien si des users existent).
3. Parcours : dashboard, produits (dropdowns *lite*), mouvements (export),
   fournisseurs, catégories, utilisateurs (ADMIN), détail produit,
   annulation de mouvement, changement de mot de passe.
4. Côté BE, `GET https://<domaine-BE>/api/dashboard/stats` sans token →
   JSON 401 (enveloppe `{timestamp,status,errors}`).

## 7. Suite

- Les pushes sur `master` déclenchent la CI des deux dépôts
  (`lint+build+test` FE, `mvn verify` BE) — visible dans l'onglet *Actions*
  de chaque repo GitHub.
- Rollback : Railway → déploiement précédent (DB untouched par un rollback
  d'app ; les migrations Flyway sont datées et non annulées — d'où le
  snapshot de l'étape 0).
