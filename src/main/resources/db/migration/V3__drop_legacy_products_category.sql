-- V3 : supprime la colonne legacy "products.category" (varchar NOT NULL sans défaut).
--
-- Contexte : l'ancienne application stockait le nom de catégorie en texte dans
-- "category". L'entité actuelle n'écrit que "category_id" (FK → categories),
-- colonne renseignée sur toutes les lignes. Du coup tout INSERT de produit
-- omettait "category" et échouait sur la contrainte NOT NULL (SQLState 23502),
-- traduit en 409 "Conflit de données : l'enregistrement existe déjà ou est
-- encore référencé" par GlobalExceptionHandler.
--
-- Idempotent : DROP COLUMN IF EXISTS (l'exécution manuelle préalable sur la
-- base de production ne casse rien au premier boot Flyway).

-- 1) Création des catégories manquantes à partir de l'ancien texte, mais
--    uniquement pour les produits encore non rattachés (no-op en pratique :
--    la migration de l'ancien schéma a déjà tout relié).
INSERT INTO categories (name, created_date)
SELECT DISTINCT p.category, now()
FROM products p
WHERE p.category_id IS NULL
  AND p.category IS NOT NULL
  AND p.category <> ''
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE lower(c.name) = lower(p.category));

-- 2) Rattachement des produits orphelins à leur catégorie (sécurité).
UPDATE products p
SET category_id = c.id
FROM categories c
WHERE p.category_id IS NULL
  AND c.name = p.category;

-- 3) Suppression de la colonne legacy (l'entité ne la mappe pas).
ALTER TABLE products DROP COLUMN IF EXISTS category;
