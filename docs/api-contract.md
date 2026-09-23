# Contrat d'API — Stock Management (FE ↔ BE)

Ce document fait foi pour les échanges entre `stock-management-frontend` et
`stock-management-backend`. Toute évolution d'endpoint, de code de statut ou
d'enveloppe doit être répercutée ici **avant** d'être implémentée.

## 1. Conventions

| Sujet        | Règle                                                                 |
| ------------ | --------------------------------------------------------------------- |
| Base URL     | `${API_URL}/api` — dev : `http://localhost:8080/api`                   |
| Auth         | `Authorization: Bearer <JWT>` sur tous les endpoints sauf `POST /auth/login` |
| Langue       | Messages destinés à l'utilisateur (`message`, `errors`) en **français** |
| Devise       | Valeurs monétaires en **MRU** dans l'API et l'UI ; en base **MRO** (×10) |
| Dates        | ISO-8601 sans offset (ex. `2026-09-23T12:00:00`)                       |
| Nulls        | Champs optionnels absents ou `null`                                    |

Les formes de ressources sont définies par les couples :
DTO Java (`com.montagegold.stock.dto.*`) ↔ modèles TS
(`src/app/core/models/*.models.ts`). Chaque champ ajouté/renommé doit l'être
des deux côtés dans le même commit.

## 2. Enveloppes de réponse

### 2.1 Succès

- `200` / `201` : corps = ressource (DTO), `PageResponse<T>` ou `T[]`
- `204` : aucun corps

### 2.2 Erreurs métier et générales — `{ timestamp, status, message }`

```json
{
  "timestamp": "2026-09-23T12:00:00",
  "status": 404,
  "message": "Produit introuvable (id=42)"
}
```

Produite par `GlobalExceptionHandler` :
- `BusinessException` (statut arbitraire : 400/404/409…)
- ressources/paramètres inconnus (404, 405)
- 500 : message générique, **jamais** de détail interne (détail journalisé côté serveur)

### 2.3 Erreurs de validation — `{ timestamp, status, errors }`

```json
{
  "timestamp": "2026-09-23T12:00:00",
  "status": 400,
  "errors": { "password": "Le mot de passe est requis" }
}
```

Clé = nom du champ JSON ; valeur = message Bean Validation (français).

### 2.4 Authentification (401/403 en JSON)

- `401` jeton absent/invalide/expiré → `RestAuthenticationEntryPoint`
- `403` rôle insuffisant → `RestAccessDeniedHandler`
- Même enveloppe `{ timestamp, status, message }`
- Côté FE, `jwtInterceptor` déconnecte sur tout `401` **hors** `/auth/login`

## 3. Pagination — `PageResponse<T>`

```json
{
  "content": [],
  "totalElements": 57,
  "totalPages": 6,
  "size": 10,
  "number": 5
}
```

- Le BE renvoie Spring `Page<T>` sérialisé **à plat** (format actuel, compatible FE).
- ⚠️ **Ne pas** activer `PageSerializationMode.VIA_DTO` : le format imbriqué
  (`page.number`, `page.totalElements`…) casserait le FE.
- Paramètres : `page` (0-based, défaut `0`), `size` (défaut `10`),
  `search`, `sortBy`, `sortDir` (`asc` | `desc`).
- L'export Excel (`/movements/export`) n'est **jamais** paginé ni tronqué.

## 4. Codes HTTP

| Code | Usage                                                                    |
| ---- | ------------------------------------------------------------------------ |
| 200  | Lecture / mise à jour réussies                                            |
| 201  | Création (produits, catégories, fournisseurs, utilisateurs, mouvements)   |
| 204  | Suppression, activation/désactivation d'un compte                         |
| 400  | Validation, règle métier (stock insuffisant, auto-désactivation…)        |
| 401  | Non authentifié, ou identifiants incorrects (`/auth/login`)              |
| 403  | Authentifié mais rôle insuffisant                                        |
| 404  | Ressource inconnue                                                       |
| 409  | Conflit (doublon, entité référencée par des mouvements/produits)         |
| 500  | Erreur interne (message générique + journal serveur)                     |

## 5. Endpoints

### 5.1 Auth — `/api/auth`

| Méthode | Chemin               | Corps                                | Succès    | Erreurs          |
| ------- | -------------------- | ------------------------------------ | --------- | ---------------- |
| POST    | `/auth/login`        | `{ username, password }`             | 200 `AuthResponse` | 400, **401** |
| POST    | `/auth/change-password` | `{ currentPassword, newPassword }` | 200 vide | 400, 401         |

`AuthResponse` : `{ token, type, id, username, fullName, role, mustChangePassword }`.

### 5.2 Produits — `/api/products`

| Méthode | Chemin                | Paramètres / Corps                      | Succès       | Erreurs          |
| ------- | --------------------- | --------------------------------------- | ------------ | ---------------- |
| GET     | `/products`           | `search, page, size, sortBy, sortDir`   | 200 `PageResponse<Product>` | — |
| GET     | `/products/next-reference` | —                                   | 200 `{ reference }` (aperçu) | — |
| GET     | `/products/{id}`      | —                                       | 200 `Product` | 404 |
| POST    | `/products`           | `ProductRequest` (référence fournie par le client) | **201** `Product` | 400, **409** (doublon) |
| PUT     | `/products/{id}`      | `ProductRequest`                        | 200 `Product` | 400, 404, 409 |
| DELETE  | `/products/{id}`      | —                                       | 204 | 404, 409 (mouvements liés) |
| GET     | `/products/alerts`    | —                                       | 200 `Product[]` | — *(à migrer vers `/api/dashboard/alerts`, cf. phase WS-B)* |
| POST    | `/products/import`    | multipart `file` (.xlsx)                | 200 `{ created, skipped, total }` | 400 |
| GET     | `/products/export/template` | —                                 | 200 `.xlsx` | — |

### 5.3 Catégories — `/api/categories`

| Méthode | Chemin             | Paramètres / Corps                | Succès           | Erreurs        |
| ------- | ------------------ | --------------------------------- | ---------------- | -------------- |
| GET     | `/categories`      | `search, page, size, sortBy, sortDir` | 200 `PageResponse<Category>` | — |
| GET     | `/categories/all`  | —                                 | 200 `Category[]` | — |
| GET     | `/categories/{id}` | —                                 | 200 `Category`   | 404 |
| POST    | `/categories`      | `CategoryRequest`                 | **201**          | 400, 409 |
| PUT     | `/categories/{id}` | `CategoryRequest`                 | 200              | 400, 404, 409 |
| DELETE  | `/categories/{id}` | —                                 | 204              | 404, 409 (produits liés) |

### 5.4 Fournisseurs — `/api/suppliers`

| Méthode | Chemin              | Paramètres / Corps                 | Succès          | Erreurs        |
| ------- | ------------------- | ---------------------------------- | --------------- | -------------- |
| GET     | `/suppliers`        | `search, page, size, sortBy, sortDir` | 200 `PageResponse<Supplier>` | — |
| GET     | `/suppliers/{id}`   | —                                  | 200 `Supplier`  | 404 |
| POST    | `/suppliers`        | `SupplierRequest`                  | **201**         | 400, 409 (NIF) |
| PUT     | `/suppliers/{id}`   | `SupplierRequest`                  | 200             | 400, 404, 409 |
| DELETE  | `/suppliers/{id}`   | —                                  | 204             | 404, 409 (mouvements liés) |
| POST    | `/suppliers/import` | multipart `file` (.xlsx)           | 200 `{ created, skipped, total }` | 400 |
| GET     | `/suppliers/export/template` | —                             | 200 `.xlsx`     | — |

### 5.5 Mouvements — `/api/movements`

| Méthode | Chemin              | Paramètres / Corps                                              | Succès           | Erreurs |
| ------- | ------------------- | --------------------------------------------------------------- | ---------------- | ------- |
| GET     | `/movements`        | `page, size, productId?, type?` (tri `movementDate` DESC)        | 200 `PageResponse<StockMovement>` | — |
| POST    | `/movements`        | `{ productId, type, quantity, reason?, externalReference?, supplierId?, recipient?, unitPrice? }` | **201** `StockMovement` | 400 (stock insuffisant, fournisseur requis en entrée, destinataire requis en sortie) |
| POST    | `/movements/{id}/cancel` | `{ reason? }`                                              | 200 `StockMovement` | 400 (mouvement de correction, déjà corrigé, stock insuffisant), 404 |
| GET     | `/movements/export` | `productId?, type?` — export **complet, non tronqué**            | 200 `.xlsx`      | — |

### 5.6 Utilisateurs — `/api/users` (rôle `ADMIN`)

| Méthode | Chemin                  | Corps                                        | Succès    | Erreurs |
| ------- | ----------------------- | -------------------------------------------- | --------- | ------- |
| GET     | `/users`                | —                                            | 200 `User[]` | 401, 403 |
| POST    | `/users`                | `{ username, password, fullName, role, active }` | **201** `User` | 400, 409 |
| PUT     | `/users/{id}`           | `{ username, fullName, role, active, password? }` | 200 `User` | 400, 404, 409 |
| PATCH   | `/users/{id}/toggle-active` | —                                        | 204       | 400 (auto-désactivation interdite, dernier admin actif) |
| DELETE  | `/users/{id}`           | —                                            | 204       | 400 (soi-même, dernier admin), 404, 409 (mouvements liés) |

Règles `password` : absent/`null` à la mise à jour ⇒ mot de passe **conservé** ;
fourni ⇒ encodé et `mustChangePassword` repasse à `true`.

### 5.7 Tableau de bord — `/api/dashboard`

| Méthode | Chemin              | Succès                          |
| ------- | ------------------- | ------------------------------- |
| GET     | `/dashboard/stats`  | 200 `DashboardStats`            |
| GET     | `/products/alerts`  | 200 `Product[]` (stock ≤ seuil) — voir 5.2 |

`DashboardStats` : `{ totalProducts, totalQuantity, productsInAlert, stockValue, monthlyEntries, monthlyExits }`.

## 6. Rôles

- `ADMIN` : toutes les fonctionnalités, y compris `/api/users/**`.
- `MANAGEMENT` : gestion des stocks (produits, mouvements, fournisseurs,
  catégories, tableau de bord), **sans** accès `/api/users/**`.
- Toute requête sans jeton valide ⇒ `401` JSON ; rôle insuffisant ⇒ `403` JSON.

## 7. Contraintes FE associées

- `apiErrorMessage` (`core/http/api-error.ts`) lit uniquement `message` puis
  `errors` de l'enveloppe — ne pas changer ces clés.
- `jwtInterceptor` : joint le jeton et déconnecte sur `401` hors login.
- Les créations renvoient `201` : le FE ne teste que le succès réseau
  (aucun `status === 200` à l'écoute), conserver cette tolérance.
- Téléchargements (`.xlsx`) via `downloadBlob` (`core/http/download.ts`).
